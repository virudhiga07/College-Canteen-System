
const API = "/api";

let foods = [];
let orders = [];
let cart = [];

// COMMON FUNCTIONS

const money = amount =>
    "₹" + Number(amount || 0).toFixed(2);

function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;"
    })[char]);
}

async function apiRequest(url, options = {}) {
    const response = await fetch(API + url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error || "Something went wrong");
    }

    return data;
}

// PAGE NAVIGATION

function showPage(pageName) {
    document.querySelectorAll(".page").forEach(page => {
        page.classList.remove("active");
    });

    document.querySelectorAll(".nav-link").forEach(button => {
        button.classList.remove("active");
    });

    document.getElementById(pageName)?.classList.add("active");

    document.querySelector(
        `.nav-link[data-page="${pageName}"]`
    )?.classList.add("active");

    const titles = {
        dashboard: ["Dashboard", "Welcome to VCET College Canteen"],
        food: ["Food Menu", "Manage your food items and prices"],
        orders: ["Orders & Billing", "Create customer orders and bills"],
        sales: ["Sales Analysis", "Monitor your canteen sales"]
    };

    document.getElementById("page-title").textContent =
        titles[pageName][0];

    document.getElementById("page-subtitle").textContent =
        titles[pageName][1];

    if (pageName === "dashboard") loadDashboard();
    if (pageName === "food") loadFoods();
    if (pageName === "orders") loadOrders();
    if (pageName === "sales") loadSales();
}

document.querySelectorAll(".nav-link").forEach(button => {
    button.addEventListener("click", () => {
        showPage(button.dataset.page);
    });
});

document.querySelectorAll("[data-go]").forEach(button => {
    button.addEventListener("click", () => {
        showPage(button.dataset.go);
    });
});

// FOOD MENU

async function loadFoods() {
    try {
        foods = await apiRequest("/foods");
        renderFoods();
        renderOrderFoods();
        updateDashboardStats();
    } catch (error) {
        document.getElementById("food-list").innerHTML =
            `<p class="empty-message">${escapeHTML(error.message)}</p>`;
    }
}

function renderFoods() {
    const search = document.getElementById("food-search")
        .value.toLowerCase();

    const category = document.getElementById("category-filter").value;

    const filtered = foods.filter(food => {
        const matchesName = food.itemName.toLowerCase().includes(search);
        const matchesCategory =
            category === "All" || food.category === category;

        return matchesName && matchesCategory;
    });

    const container = document.getElementById("food-list");

    if (filtered.length === 0) {
        container.innerHTML =
            `<p class="empty-message">No food items found.</p>`;
        return;
    }

    container.innerHTML = filtered.map(food => `
        <div class="food-card">
            <div class="category">${escapeHTML(food.category)}</div>

            <h3>${escapeHTML(food.itemName)}</h3>

            <div class="price">${money(food.price)}</div>

            <div class="stock">
                Stock: ${food.stockQuantity}
                ${food.stockQuantity < 10
                    ? '<span style="color:#df5656"> • Low Stock</span>'
                    : ''}
            </div>

            <div class="food-actions">
                <button class="edit-btn"
                    data-edit="${food.itemId}">
                    Edit
                </button>

                <button class="delete-btn"
                    data-delete="${food.itemId}">
                    Delete
                </button>
            </div>
        </div>
    `).join("");
}

document.getElementById("food-search")
    .addEventListener("input", renderFoods);

document.getElementById("category-filter")
    .addEventListener("change", renderFoods);

// ADD FOOD MODAL

const modal = document.getElementById("food-modal");
const foodForm = document.getElementById("food-form");

function openFoodModal(food = null) {
    foodForm.reset();

    document.getElementById("edit-food-id").value =
        food ? food.itemId : "";

    document.getElementById("modal-title").textContent =
        food ? "Edit Food Item" : "Add Food Item";

    if (food) {
        document.getElementById("food-name").value = food.itemName;
        document.getElementById("food-price").value = food.price;
        document.getElementById("food-stock").value =
            food.stockQuantity;
        document.getElementById("food-category").value =
            food.category || "Other";
    }

    modal.classList.add("show");
}

function closeFoodModal() {
    modal.classList.remove("show");
}

document.getElementById("add-food-btn")
    .addEventListener("click", () => openFoodModal());

document.getElementById("close-modal")
    .addEventListener("click", closeFoodModal);

modal.addEventListener("click", event => {
    if (event.target === modal) {
        closeFoodModal();
    }
});

// SAVE FOOD

foodForm.addEventListener("submit", async event => {
    event.preventDefault();

    const id = document.getElementById("edit-food-id").value;

    const food = {
        itemName: document.getElementById("food-name").value.trim(),
        price: Number(document.getElementById("food-price").value),
        stockQuantity: Number(
            document.getElementById("food-stock").value
        ),
        category: document.getElementById("food-category").value
    };

    if (!food.itemName || food.price <= 0 ||
        !Number.isFinite(food.price) ||
        !Number.isInteger(food.stockQuantity) ||
        food.stockQuantity < 0) {
        alert("Please enter valid food details.");
        return;
    }

    try {
        if (id) {
            await apiRequest(`/foods/${id}`, {
                method: "PUT",
                body: JSON.stringify(food)
            });

            alert("Food item updated successfully!");
        } else {
            await apiRequest("/foods", {
                method: "POST",
                body: JSON.stringify(food)
            });

            alert("Food item added successfully!");
        }

        closeFoodModal();
        await loadFoods();

    } catch (error) {
        alert(error.message);
    }
});

// EDIT AND DELETE FOOD

document.getElementById("food-list")
    .addEventListener("click", async event => {

        const editButton = event.target.closest("[data-edit]");
        const deleteButton = event.target.closest("[data-delete]");

        if (editButton) {
            const id = Number(editButton.dataset.edit);
            const food = foods.find(item => item.itemId === id);

            if (food) openFoodModal(food);
        }

        if (deleteButton) {
            const id = Number(deleteButton.dataset.delete);
            const food = foods.find(item => item.itemId === id);

            if (!food) return;

            const confirmed = confirm(
                `Delete ${food.itemName}?`
            );

            if (!confirmed) return;

            try {
                await apiRequest(`/foods/${id}`, {
                    method: "DELETE"
                });

                alert("Food item deleted successfully!");
                await loadFoods();

            } catch (error) {
                alert(error.message);
            }
        }
    });

// ORDERS AND BILLING

function renderOrderFoods() {
    const search = document.getElementById("order-search")
        .value.toLowerCase();

    const filtered = foods.filter(food =>
        food.itemName.toLowerCase().includes(search)
    );

    const container = document.getElementById("order-food-list");

    if (filtered.length === 0) {
        container.innerHTML =
            `<p class="empty-message">No food items found.</p>`;
        return;
    }

    container.innerHTML = filtered.map(food => `
        <div class="order-food-item">
            <strong>${escapeHTML(food.itemName)}</strong>

            <span>${money(food.price)}</span>

            <small>Stock: ${food.stockQuantity}</small>

            <button data-add-cart="${food.itemId}"
                ${food.stockQuantity <= 0 ? "disabled" : ""}>
                ${food.stockQuantity <= 0 ? "Out of Stock" : "+ Add to Cart"}
            </button>
        </div>
    `).join("");
}

document.getElementById("order-search")
    .addEventListener("input", renderOrderFoods);

// ADD TO CART

document.getElementById("order-food-list")
    .addEventListener("click", event => {

        const button = event.target.closest("[data-add-cart]");

        if (!button) return;

        const id = Number(button.dataset.addCart);
        const food = foods.find(item => item.itemId === id);

        if (!food || food.stockQuantity <= 0) return;

        const existing = cart.find(item => item.itemId === id);

        const currentQuantity = existing ? existing.quantity : 0;

        if (currentQuantity >= food.stockQuantity) {
            alert("Available stock limit reached.");
            return;
        }

        if (existing) {
            existing.quantity++;
        } else {
            cart.push({
                itemId: food.itemId,
                itemName: food.itemName,
                price: food.price,
                quantity: 1
            });
        }

        renderCart();
    });

// DISPLAY CART

function renderCart() {
    const container = document.getElementById("cart-items");

    if (cart.length === 0) {
        container.innerHTML =
            `<p class="empty-message">Your cart is empty.</p>`;
    } else {
        container.innerHTML = cart.map(item => `
            <div class="cart-item">
                <div>
                    <strong>${escapeHTML(item.itemName)}</strong>
                    <small>${money(item.price)} × ${item.quantity}</small>
                </div>

                <div class="quantity-controls">
                    <button data-minus="${item.itemId}">−</button>

                    <span>${item.quantity}</span>

                    <button data-plus="${item.itemId}">+</button>
                </div>
            </div>
        `).join("");
    }

    const total = cart.reduce(
        (sum, item) => sum + item.price * item.quantity, 0
    );

    document.getElementById("cart-total").textContent = money(total);
}

// CART QUANTITY CONTROLS

document.getElementById("cart-items")
    .addEventListener("click", event => {

        const plusButton = event.target.closest("[data-plus]");
        const minusButton = event.target.closest("[data-minus]");

        if (!plusButton && !minusButton) return;

        const id = Number(
            plusButton
                ? plusButton.dataset.plus
                : minusButton.dataset.minus
        );

        const item = cart.find(food => food.itemId === id);
        const food = foods.find(food => food.itemId === id);

        if (!item) return;

        if (plusButton) {
            if (item.quantity >= food.stockQuantity) {
                alert("Available stock limit reached.");
                return;
            }

            item.quantity++;
        }

        if (minusButton) {
            item.quantity--;

            if (item.quantity <= 0) {
                cart = cart.filter(food => food.itemId !== id);
            }
        }

        renderCart();
    });

// GENERATE BILL AND PLACE ORDER

document.getElementById("place-order-btn")
    .addEventListener("click", async () => {

        if (cart.length === 0) {
            alert("Please add food items to your cart.");
            return;
        }

        const button = document.getElementById("place-order-btn");
        button.disabled = true;
        button.textContent = "Processing...";

        try {
            const result = await apiRequest("/orders", {
                method: "POST",
                body: JSON.stringify({
                    items: cart.map(item => ({
                        itemId: item.itemId,
                        quantity: item.quantity
                    }))
                })
            });

            showBill(result);

            cart = [];
            renderCart();

            await loadFoods();
            await loadOrders();
            await loadDashboard();
            await loadSales();

        } catch (error) {
            alert(error.message);
            await loadFoods();

        } finally {
            button.disabled = false;
            button.textContent = "Generate Bill";
        }
    });

// DISPLAY BILL

function showBill(bill) {
    const container = document.getElementById("bill-result");

    const rows = bill.items.map(item => `
        <p>
            ${escapeHTML(item.itemName)}
            × ${item.quantity}
            = ${money(item.subtotal)}
        </p>
    `).join("");

    container.innerHTML = `
        <div class="bill-box">
            <h3>VCET College Canteen</h3>

            <p><strong>Order ID:</strong> #${bill.orderId}</p>

            <p><strong>Date:</strong>
                ${escapeHTML(new Date(bill.orderDate).toLocaleString())}
            </p>

            <hr>

            ${rows}

            <hr>

            <h3>Total: ${money(bill.total)}</h3>

            <button class="print-btn" id="print-bill">
                Print Bill
            </button>
        </div>
    `;

    document.getElementById("print-bill")
        .addEventListener("click", () => window.print());
}

// ORDER HISTORY

async function loadOrders() {
    try {
        orders = await apiRequest("/orders");
        renderOrders();
    } catch (error) {
        console.error(error);
    }
}

function renderOrders() {
    const rows = orders.map(order => `
        <tr>
            <td>#${order.orderId}</td>
            <td>${escapeHTML(
                new Date(order.orderDate).toLocaleString()
            )}</td>
            <td>${money(order.total)}</td>
        </tr>
    `).join("");

    const emptyRow = `
        <tr>
            <td colspan="3" class="empty-message">No orders yet</td>
        </tr>
    `;

    document.getElementById("all-orders").innerHTML =
        rows || emptyRow;

    document.getElementById("recent-orders").innerHTML =
        rows ? orders.slice(0, 5).map(order => `
            <tr>
                <td>#${order.orderId}</td>
                <td>${escapeHTML(
                    new Date(order.orderDate).toLocaleString()
                )}</td>
                <td>${money(order.total)}</td>
            </tr>
        `).join("") : emptyRow;
}

// DASHBOARD

async function loadDashboard() {
    try {
        await Promise.all([
            loadFoods(),
            loadOrders()
        ]);

        const sales = await apiRequest("/sales");

        document.getElementById("total-sales").textContent =
            money(sales.totalSales);

        document.getElementById("total-orders").textContent =
            sales.totalOrders;

    } catch (error) {
        console.error("Dashboard:", error);
    }
}

function updateDashboardStats() {
    document.getElementById("total-foods").textContent =
        foods.length;

    const lowStock = foods.filter(
        food => food.stockQuantity < 10
    ).length;

    document.getElementById("low-stock").textContent = lowStock;
}

// SALES ANALYSIS

async function loadSales() {
    try {
        const sales = await apiRequest("/sales");

        document.getElementById("sales-revenue").textContent =
            money(sales.totalSales);

        document.getElementById("sales-orders").textContent =
            sales.totalOrders;

        const container = document.getElementById("sales-table");

        if (!sales.items || sales.items.length === 0) {
            container.innerHTML = `
                <tr>
                    <td colspan="3" class="empty-message">
                        No sales data available
                    </td>
                </tr>
            `;
            return;
        }

        container.innerHTML = sales.items.map(item => `
            <tr>
                <td>${escapeHTML(item.itemName)}</td>
                <td>${item.quantitySold}</td>
                <td>${money(item.revenue)}</td>
            </tr>
        `).join("");

    } catch (error) {
        alert("Unable to load sales report: " + error.message);
    }
}

document.getElementById("refresh-sales")
    .addEventListener("click", loadSales);

// START WEBSITE

async function startApplication() {
    try {
        await loadFoods();
        await loadOrders();
        await loadDashboard();

        console.log("VCET Canteen website connected successfully.");

    } catch (error) {
        console.error("Application startup error:", error);
    }
}

startApplication();
