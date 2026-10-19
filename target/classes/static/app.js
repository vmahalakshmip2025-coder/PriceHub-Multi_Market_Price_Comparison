/**
 * PriceHub - Frontend Single Page Architecture
 * Vanilla JavaScript (No Frameworks or External Libraries)
 */

const API_BASE = '/api';

// Application State Store
const state = {
    products: [],
    filteredProducts: [],
    activeCategory: 'ALL',
    activeSort: 'featured',
    searchQuery: '',
    selectedProductComparison: null,
    theme: localStorage.getItem('pricehub_theme') || 'light'
};

// SVG Placeholder generator for fallback images
function getCategoryPlaceholder(category) {
    const text = category ? category.replace('_', ' ') : 'PRODUCT';
    const svg = `
        <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200" viewBox="0 0 300 200">
            <rect width="300" height="200" fill="#e2e8f0"/>
            <text x="50%" y="45%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="32" fill="#94a3b8">📦</text>
            <text x="50%" y="70%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="12" font-weight="600" fill="#64748b">${text}</text>
        </svg>
    `.trim();
    return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
}

// Global App Controller
const app = {
    init() {
        this.applyTheme(state.theme);
        this.bindEvents();
        this.loadCatalog();
    },

    bindEvents() {
        // Theme toggle
        document.getElementById('themeToggleBtn').addEventListener('click', () => {
            const nextTheme = state.theme === 'light' ? 'dark' : 'light';
            this.applyTheme(nextTheme);
        });

        // Search input with instant debounce
        const searchInput = document.getElementById('searchInput');
        const clearBtn = document.getElementById('clearSearchBtn');

        let debounceTimer;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            const val = e.target.value.trim();
            state.searchQuery = val;
            clearBtn.classList.toggle('hidden', val.length === 0);

            debounceTimer = setTimeout(() => {
                this.filterAndRender();
            }, 250);
        });

        clearBtn.addEventListener('click', () => {
            searchInput.value = '';
            state.searchQuery = '';
            clearBtn.classList.add('hidden');
            this.filterAndRender();
        });

        // Category chips
        document.getElementById('categoryChips').addEventListener('click', (e) => {
            if (e.target.classList.contains('chip')) {
                document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
                e.target.classList.add('active');
                state.activeCategory = e.target.dataset.category;
                this.filterAndRender();
            }
        });

        // Sort select
        document.getElementById('sortSelect').addEventListener('change', (e) => {
            state.activeSort = e.target.value;
            this.filterAndRender();
        });
    },

    applyTheme(theme) {
        state.theme = theme;
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('pricehub_theme', theme);
        document.getElementById('themeIcon').textContent = (theme === 'dark') ? '☀️' : '🌙';
    },

    // ================= Catalog Operations =================
    async loadCatalog() {
        this.setCatalogLoading(true);
        this.setCatalogError(false);

        try {
            const response = await fetch(`${API_BASE}/products`);
            if (!response.ok) {
                throw new Error(`Catalog load failed: HTTP ${response.status}`);
            }
            const data = await response.json();
            state.products = data;
            this.filterAndRender();
        } catch (err) {
            console.error('Error fetching catalog:', err);
            this.setCatalogError(true, err.message);
        } finally {
            this.setCatalogLoading(false);
        }
    },

    filterAndRender() {
        let results = [...state.products];

        // 1. Filter by category
        if (state.activeCategory !== 'ALL') {
            results = results.filter(p => p.category === state.activeCategory);
        }

        // 2. Filter by search query
        if (state.searchQuery) {
            const q = state.searchQuery.toLowerCase();
            results = results.filter(p => p.name.toLowerCase().includes(q));
        }

        // 3. Sort
        results.sort((a, b) => {
            switch (state.activeSort) {
                case 'price-asc':
                    return (a.lowestPrice || 999999) - (b.lowestPrice || 999999);
                case 'price-desc':
                    return (b.lowestPrice || 0) - (a.lowestPrice || 0);
                case 'rating-desc':
                    return (b.averageRating || 0) - (a.averageRating || 0);
                case 'name-asc':
                    return a.name.localeCompare(b.name);
                default:
                    return a.id - b.id;
            }
        });

        state.filteredProducts = results;
        this.renderCatalogGrid(results);
    },

    renderCatalogGrid(products) {
        const grid = document.getElementById('productsGrid');
        const emptyState = document.getElementById('catalogEmpty');

        grid.innerHTML = '';

        if (products.length === 0) {
            emptyState.classList.remove('hidden');
            return;
        }

        emptyState.classList.add('hidden');

        products.forEach(p => {
            const card = document.createElement('div');
            card.className = 'product-card';
            card.onclick = () => this.showComparisonView(p.id);

            const ratingHtml = p.averageRating
                ? `<div class="rating-badge">★ ${p.averageRating}</div>`
                : `<div class="rating-badge" style="background-color: var(--text-muted);">★ Unrated</div>`;

            const priceFormatted = p.lowestPrice != null ? `₹${p.lowestPrice}` : 'Out of stock';
            const placeholder = getCategoryPlaceholder(p.category);
            const imgSrc = p.imageUrl && p.imageUrl.trim().length > 0 ? p.imageUrl : placeholder;

            card.innerHTML = `
                <div class="product-image-container">
                    <img src="${imgSrc}" alt="${p.name}" loading="lazy" onerror="this.onerror=null; this.src='${placeholder}';">
                </div>
                <div class="product-content">
                    <span class="badge-category">${p.categoryDisplayName}</span>
                    <h3 class="product-title" title="${p.name}">${p.name}</h3>
                    ${ratingHtml}
                    <div class="product-footer">
                        <div class="price-box">
                            <span class="price-caption">Lowest from:</span>
                            <span class="price-value">${priceFormatted}</span>
                        </div>
                        <button class="btn-compare" onclick="event.stopPropagation(); app.showComparisonView(${p.id})">
                            Compare
                        </button>
                    </div>
                </div>
            `;
            grid.appendChild(card);
        });
    },

    resetFilters() {
        state.activeCategory = 'ALL';
        state.searchQuery = '';
        state.activeSort = 'featured';
        document.getElementById('searchInput').value = '';
        document.getElementById('clearSearchBtn').classList.add('hidden');
        document.getElementById('sortSelect').value = 'featured';
        document.querySelectorAll('.chip').forEach(c => {
            c.classList.toggle('active', c.dataset.category === 'ALL');
        });
        this.filterAndRender();
    },

    // ================= Comparison View Operations =================
    async showComparisonView(productId) {
        document.getElementById('catalogView').classList.add('hidden');
        const compView = document.getElementById('comparisonView');
        compView.classList.remove('hidden');
        window.scrollTo({ top: 0, behavior: 'smooth' });

        const loading = document.getElementById('comparisonLoading');
        const content = document.getElementById('comparisonContent');
        const errorState = document.getElementById('comparisonError');

        loading.classList.remove('hidden');
        content.classList.add('hidden');
        errorState.classList.add('hidden');

        try {
            // Fetch comparison, alternatives, and historical audits concurrently
            const [compareRes, altRes, historyRes] = await Promise.all([
                fetch(`${API_BASE}/products/${productId}/compare`),
                fetch(`${API_BASE}/products/${productId}/alternatives`),
                fetch(`${API_BASE}/products/${productId}/history`)
            ]);

            if (!compareRes.ok) throw new Error('Failed to load comparison data');
            const compareData = await compareRes.json();
            const altData = altRes.ok ? await altRes.json() : [];
            const historyData = historyRes.ok ? await historyRes.json() : [];

            state.selectedProductComparison = compareData;

            this.renderComparisonDetails(compareData);
            this.renderBestValueBanner(compareData.bestValue);
            this.renderMarketplaceCards(compareData.marketplaces, compareData.productName);
            this.renderPriceBarChart(compareData.marketplaces);
            this.renderHistoryChart(historyData);
            this.renderAlternatives(altData);

            loading.classList.add('hidden');
            content.classList.remove('hidden');
        } catch (err) {
            console.error('Error loading comparison:', err);
            loading.classList.add('hidden');
            errorState.classList.remove('hidden');
            document.getElementById('comparisonErrorMessage').textContent = err.message;
        }
    },

    showCatalogView() {
        document.getElementById('comparisonView').classList.add('hidden');
        document.getElementById('catalogView').classList.remove('hidden');
        state.selectedProductComparison = null;
    },

    renderComparisonDetails(data) {
        document.getElementById('detailProductName').textContent = data.productName;
        document.getElementById('detailProductBrand').textContent = data.brand;
        document.getElementById('detailProductCategory').textContent = data.categoryDisplayName;

        const placeholder = getCategoryPlaceholder(data.category);
        const imgElem = document.getElementById('detailProductImage');
        imgElem.src = data.imageUrl || placeholder;
        imgElem.onerror = () => { imgElem.src = placeholder; };

        document.getElementById('detailLowestPrice').textContent = data.lowestPrice != null ? `₹${data.lowestPrice}` : 'N/A';
        document.getElementById('detailSavings').textContent = (data.savingsAmount && data.savingsAmount > 0)
            ? `Save ₹${data.savingsAmount} (${data.savingsPercentage}%)`
            : '₹0 (No savings)';
    },

    renderBestValueBanner(bestValue) {
        const banner = document.getElementById('bestValueBanner');
        if (!bestValue) {
            banner.classList.add('hidden');
            return;
        }

        banner.classList.remove('hidden');
        document.getElementById('bestValueMarketplace').textContent = `Recommended: ${bestValue.displayName}`;
        document.getElementById('bestValuePrice').textContent = `₹${bestValue.price}`;
        document.getElementById('bestValueReason').textContent = bestValue.reason;
    },

    renderMarketplaceCards(marketplaces, productName) {
        const container = document.getElementById('marketplacesGrid');
        container.innerHTML = '';

        marketplaces.forEach(mp => {
            const card = document.createElement('div');
            const mpKey = mp.marketplace.toLowerCase();
            card.className = `marketplace-card ${mpKey} ${mp.isCheapest ? 'is-cheapest' : ''} ${mp.isBestValue ? 'is-best-value' : ''}`;

            // Check if price is older than 7 days
            let outdatedHtml = '';
            if (mp.priceDate) {
                const recordedDate = new Date(mp.priceDate);
                const diffDays = Math.floor((new Date() - recordedDate) / (1000 * 60 * 60 * 24));
                if (diffDays > 7) {
                    outdatedHtml = `<div class="outdated-warning">⚠️ May be outdated - check on marketplace (${diffDays} days old)</div>`;
                }
            }

            const badges = [];
            if (mp.isBestValue) badges.push('<span class="badge-tag best">Best Value</span>');
            if (mp.isCheapest) badges.push('<span class="badge-tag cheapest">Lowest Price</span>');

            const ratingDisplay = mp.rating != null ? `★ ${mp.rating}` : 'Not rated';
            const priceDisplay = mp.formattedPrice || (mp.price != null ? `₹${mp.price}` : 'Not available');
            const isUnavailable = !mp.price;

            card.innerHTML = `
                <div class="platform-header">
                    <span class="platform-title">${mp.displayName}</span>
                    <div class="card-badges">${badges.join('')}</div>
                </div>

                <div class="mp-price-row">
                    <div class="mp-price ${isUnavailable ? 'unavailable' : ''}">${priceDisplay}</div>
                </div>

                ${outdatedHtml}

                <ul class="mp-specs">
                    <li>
                        <span class="spec-key">Availability</span>
                        <span class="spec-val ${mp.inStock ? 'stock-in' : 'stock-out'}">
                            ${mp.inStock ? '● In Stock' : '✕ Out of Stock'}
                        </span>
                    </li>
                    <li>
                        <span class="spec-key">Customer Rating</span>
                        <span class="spec-val">${ratingDisplay}</span>
                    </li>
                    <li>
                        <span class="spec-key">Warranty</span>
                        <span class="spec-val">${mp.warranty || 'Warranty not listed'}</span>
                    </li>
                    <li>
                        <span class="spec-key">Price as on</span>
                        <span class="spec-val">${mp.priceDate || 'N/A'}</span>
                    </li>
                </ul>

                <a href="${mp.productUrl}" target="_blank" rel="noopener noreferrer" class="btn-deal">
                    View Deal ↗
                </a>
            `;

            container.appendChild(card);
        });
    },

    renderPriceBarChart(marketplaces) {
        const container = document.getElementById('priceBarChart');
        container.innerHTML = '';

        const validPrices = marketplaces.filter(m => m.price != null && m.price > 0);
        if (validPrices.length === 0) {
            container.innerHTML = '<p class="text-muted">No pricing data available to plot chart.</p>';
            return;
        }

        const maxPrice = Math.max(...validPrices.map(m => m.price));

        marketplaces.forEach(m => {
            const row = document.createElement('div');
            row.className = 'chart-bar-row';

            const percentage = m.price ? Math.max(Math.round((m.price / maxPrice) * 100), 15) : 0;
            const barFillClass = m.marketplace.toLowerCase();
            const valText = m.price ? `₹${m.price}` : 'N/A';

            row.innerHTML = `
                <div class="chart-bar-label">${m.displayName}</div>
                <div class="chart-bar-track">
                    <div class="chart-bar-fill ${barFillClass}" style="width: ${percentage}%">
                        ${valText}
                    </div>
                </div>
            `;
            container.appendChild(row);
        });
    },

    renderHistoryChart(history) {
        const container = document.getElementById('priceHistoryChartContainer');
        container.innerHTML = '';

        if (!history || history.length === 0) {
            container.innerHTML = '<p style="color: var(--text-muted); padding: 1rem 0;">No historical price changes recorded in dataset.</p>';
            return;
        }

        // SVG Historical Line/Step Renderer
        const validHistory = history.filter(h => h.price != null);
        if (validHistory.length === 0) {
            container.innerHTML = '<p style="color: var(--text-muted);">Historical records lack price information.</p>';
            return;
        }

        const width = 800;
        const height = 180;
        const padding = 40;

        const prices = validHistory.map(h => h.price);
        const minP = Math.min(...prices) * 0.9;
        const maxP = Math.max(...prices) * 1.1;

        // Build data points
        const points = validHistory.map((h, i) => {
            const x = padding + (i * ((width - 2 * padding) / Math.max(validHistory.length - 1, 1)));
            const y = height - padding - ((h.price - minP) / (maxP - minP || 1)) * (height - 2 * padding);
            return { x, y, item: h };
        });

        const pathD = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');

        let svgHtml = `
            <svg class="history-svg" viewBox="0 0 ${width} ${height}" preserveAspectRatio="none">
                <!-- Axes -->
                <line x1="${padding}" y1="${height - padding}" x2="${width - padding}" y2="${height - padding}" stroke="var(--border-color)" stroke-width="2"/>
                <line x1="${padding}" y1="${padding}" x2="${padding}" y2="${height - padding}" stroke="var(--border-color)" stroke-width="2"/>

                <!-- Line -->
                <path d="${pathD}" fill="none" stroke="var(--primary-color)" stroke-width="3" stroke-linecap="round"/>
        `;

        // Dots & text labels
        points.forEach(p => {
            const dateStr = p.item.recordedAt ? p.item.recordedAt.split('T')[0] : '';
            svgHtml += `
                <circle cx="${p.x}" cy="${p.y}" r="5" fill="var(--primary-color)"/>
                <text x="${p.x}" y="${p.y - 10}" font-size="11" font-weight="bold" fill="var(--text-primary)" text-anchor="middle">₹${p.item.price}</text>
                <text x="${p.x}" y="${height - padding + 18}" font-size="10" fill="var(--text-muted)" text-anchor="middle">${p.item.marketplaceName} (${dateStr})</text>
            `;
        });

        svgHtml += `</svg>`;
        container.innerHTML = svgHtml;
    },

    renderAlternatives(alternatives) {
        const container = document.getElementById('alternativesGrid');
        container.innerHTML = '';

        if (!alternatives || alternatives.length === 0) {
            container.innerHTML = '<p class="text-muted">No alternative suggestions available for this item.</p>';
            return;
        }

        alternatives.forEach(alt => {
            const card = document.createElement('div');
            card.className = 'alt-card';
            card.onclick = () => this.showComparisonView(alt.productId);

            const ratingHtml = alt.rating ? `★ ${alt.rating}` : 'Unrated';
            const priceHtml = alt.lowestPrice ? `₹${alt.lowestPrice}` : 'Check price';

            card.innerHTML = `
                <span class="badge-category" style="font-size: 0.65rem;">${alt.isFallbackOption ? 'Category Peer' : 'High Value Alternative'}</span>
                <h4 class="alt-title">${alt.productName}</h4>
                <div class="alt-reason">${alt.reason}</div>
                <div class="alt-footer">
                    <div>
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block;">Lowest on ${alt.cheapestMarketplace}</span>
                        <strong style="font-size: 1.1rem; color: var(--text-primary);">${priceHtml}</strong>
                    </div>
                    <button class="btn-compare" onclick="event.stopPropagation(); app.showComparisonView(${alt.productId})">
                        Compare
                    </button>
                </div>
            `;
            container.appendChild(card);
        });
    },

    // UI Feedback Helpers
    setCatalogLoading(loading) {
        document.getElementById('catalogLoading').classList.toggle('hidden', !loading);
    },

    setCatalogError(isError, message = '') {
        const el = document.getElementById('catalogError');
        el.classList.toggle('hidden', !isError);
        if (isError && message) {
            document.getElementById('catalogErrorMessage').textContent = message;
        }
    }
};

// Initialize Application when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    app.init();
});