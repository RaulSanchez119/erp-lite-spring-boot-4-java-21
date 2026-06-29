package com.raulsanchez.erp_lite.product;

import com.raulsanchez.erp_lite.domain.entities.product.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.raulsanchez.erp_lite.domain.entities.product.events.ProductCreated;
import com.raulsanchez.erp_lite.domain.entities.product.events.ProductDeactivated;
import com.raulsanchez.erp_lite.domain.entities.product.events.ProductUpdated;
import com.raulsanchez.erp_lite.domain.entities.product.events.StockChanged;
import com.raulsanchez.erp_lite.domain.shared.Money;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product Domain Test")
class ProductTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    @DisplayName("Should Throw IllegalArgumentException When Price Is Null")
    void shouldThrowIllegalArgumentExceptionWhenPriceIsNull() {
        final String msgEx = "Price cannot be null";

        IllegalArgumentException targetEx = assertThrows(IllegalArgumentException.class,
                () -> ProductRoot.create(
                        SKU.of("LAPTOP-001"),
                        ProductName.of("Laptop"),
                        "Description",
                        null,
                        Stock.of(10),
                        CategoryReference.of("cat-electronics"),
                        ProductImage.of("https://example.com/image.jpg"),
                        "test-user"
                ));

        assertEquals(msgEx, targetEx.getMessage());
    }

    @Test
    @DisplayName("Should Throw IllegalArgumentException When Price Is Zero Or Negative")
    void shouldThrowIllegalArgumentExceptionWhenPriceIsZeroOrNegative() {
        final String msgEx = "Price must be greater than 0";

        IllegalArgumentException targetExZero = assertThrows(IllegalArgumentException.class,
                () -> ProductRoot.create(
                        SKU.of("LAPTOP-001"),
                        ProductName.of("Laptop"),
                        "Description",
                        Money.of(0.0, USD),
                        Stock.of(10),
                        CategoryReference.of("cat-electronics"),
                        ProductImage.of("https://example.com/image.jpg"),
                        "test-user"
                ));

        assertEquals(msgEx, targetExZero.getMessage());
    }

    @Test
    @DisplayName("Should Create Product With Valid Data And Register ProductCreated Event")
    void shouldCreateProductWithValidDataAndRegisterProductCreatedEvent() {
        SKU sku = SKU.of("LAPTOP-001");
        ProductName name = ProductName.of("Laptop Computer");
        String description = "High-performance laptop";
        Money price = Money.of(999.99, USD);
        Stock stock = Stock.of(100);
        CategoryReference category = CategoryReference.of("cat-electronics");
        ProductImage image = ProductImage.of("https://example.com/laptop.jpg");

        ProductRoot product = ProductRoot.create(sku, name, description, price, stock, category, image, "test-user");

        assertNotNull(product.getId());
        assertEquals(sku, product.getSku());
        assertEquals(name, product.getName());
        assertEquals(description, product.getDescription());
        assertEquals(price, product.getPrice());
        assertEquals(stock, product.getStock());
        assertEquals(category, product.getCategory());
        assertEquals(image, product.getImage());
        assertTrue(product.isActive());
        assertNotNull(product.getAuditInfo());

        // Verify event registration
        assertEquals(1, product.getDomainEvents().size());
        assertTrue(product.getDomainEvents().get(0) instanceof ProductCreated);
    }

    @Test
    @DisplayName("Should Update Product Information And Register ProductUpdated Event")
    void shouldUpdateProductInformationAndRegisterProductUpdatedEvent() {
        ProductRoot product = createValidProduct();
        product.clearDomainEvents();

        ProductName newName = ProductName.of("Updated Laptop");
        String newDescription = "Updated description";
        Money newPrice = Money.of(1099.99, USD);
        CategoryReference newCategory = CategoryReference.of("cat-computers");
        ProductImage newImage = ProductImage.of("https://example.com/new-laptop.jpg");

        product.update(newName, newDescription, newPrice, newCategory, newImage);

        assertEquals(newName, product.getName());
        assertEquals(newDescription, product.getDescription());
        assertEquals(newPrice, product.getPrice());
        assertEquals(newCategory, product.getCategory());
        assertEquals(newImage, product.getImage());

        // Verify event registration
        assertEquals(1, product.getDomainEvents().size());
        assertTrue(product.getDomainEvents().get(0) instanceof ProductUpdated);
    }

    @Test
    @DisplayName("Should Increment Stock And Register StockChanged Event")
    void shouldIncrementStockAndRegisterStockChangedEvent() {
        ProductRoot product = createValidProduct();
        product.clearDomainEvents();

        int initialStock = product.getStock().value();
        String reason = "Restocking from supplier";

        product.incrementStock(50, reason);

        assertEquals(initialStock + 50, product.getStock().value());

        // Verify event registration
        assertEquals(1, product.getDomainEvents().size());
        StockChanged event = (StockChanged) product.getDomainEvents().get(0);
        assertEquals(initialStock, event.oldStock());
        assertEquals(initialStock + 50, event.newStock());
        assertEquals(reason, event.reason());
    }

    @Test
    @DisplayName("Should Throw IllegalArgumentException When Increment Reason Is Null Or Blank")
    void shouldThrowIllegalArgumentExceptionWhenIncrementReasonIsNullOrBlank() {
        ProductRoot product = createValidProduct();

        IllegalArgumentException targetExNull = assertThrows(IllegalArgumentException.class,
                () -> product.incrementStock(10, null));

        assertEquals("Reason for stock increment cannot be null or blank", targetExNull.getMessage());

        IllegalArgumentException targetExBlank = assertThrows(IllegalArgumentException.class,
                () -> product.incrementStock(10, "   "));

        assertEquals("Reason for stock increment cannot be null or blank", targetExBlank.getMessage());
    }

    @Test
    @DisplayName("Should Decrement Stock And Register StockChanged Event")
    void shouldDecrementStockAndRegisterStockChangedEvent() {
        ProductRoot product = createValidProduct();
        product.clearDomainEvents();

        int initialStock = product.getStock().value();
        String reason = "Sold items";

        product.decrementStock(20, reason);

        assertEquals(initialStock - 20, product.getStock().value());

        // Verify event registration
        assertEquals(1, product.getDomainEvents().size());
        StockChanged event = (StockChanged) product.getDomainEvents().get(0);
        assertEquals(initialStock, event.oldStock());
        assertEquals(initialStock - 20, event.newStock());
        assertEquals(reason, event.reason());
    }

    @Test
    @DisplayName("Should Throw IllegalArgumentException When Decrement Reason Is Null Or Blank")
    void shouldThrowIllegalArgumentExceptionWhenDecrementReasonIsNullOrBlank() {
        ProductRoot product = createValidProduct();

        IllegalArgumentException targetExNull = assertThrows(IllegalArgumentException.class,
                () -> product.decrementStock(10, null));

        assertEquals("Reason for stock decrement cannot be null or blank", targetExNull.getMessage());

        IllegalArgumentException targetExBlank = assertThrows(IllegalArgumentException.class,
                () -> product.decrementStock(10, ""));

        assertEquals("Reason for stock decrement cannot be null or blank", targetExBlank.getMessage());
    }

    @Test
    @DisplayName("Should Change Price And Register ProductUpdated Event")
    void shouldChangePriceAndRegisterProductUpdatedEvent() {
        ProductRoot product = createValidProduct();
        product.clearDomainEvents();

        Money newPrice = Money.of(1199.99, USD);

        product.changePrice(newPrice);

        assertEquals(newPrice, product.getPrice());

        // Verify event registration
        assertEquals(1, product.getDomainEvents().size());
        assertTrue(product.getDomainEvents().get(0) instanceof ProductUpdated);
    }

    @Test
    @DisplayName("Should Throw IllegalArgumentException When Changing To Null Or Invalid Price")
    void shouldThrowIllegalArgumentExceptionWhenChangingToNullOrInvalidPrice() {
        ProductRoot product = createValidProduct();

        IllegalArgumentException targetExNull = assertThrows(IllegalArgumentException.class,
                () -> product.changePrice(null));

        assertEquals("Price cannot be null", targetExNull.getMessage());

        IllegalArgumentException targetExZero = assertThrows(IllegalArgumentException.class,
                () -> product.changePrice(Money.of(0.0, USD)));

        assertEquals("Price must be greater than 0", targetExZero.getMessage());
    }

    @Test
    @DisplayName("Should Deactivate Product And Register ProductDeactivated Event")
    void shouldDeactivateProductAndRegisterProductDeactivatedEvent() {
        ProductRoot product = createValidProduct();
        product.clearDomainEvents();

        assertTrue(product.isActive());

        product.deactivate();

        assertFalse(product.isActive());

        // Verify event registration
        assertEquals(1, product.getDomainEvents().size());
        assertTrue(product.getDomainEvents().get(0) instanceof ProductDeactivated);
    }

    @Test
    @DisplayName("Should Throw IllegalStateException When Deactivating Already Deactivated Product")
    void shouldThrowIllegalStateExceptionWhenDeactivatingAlreadyDeactivatedProduct() {
        ProductRoot product = createValidProduct();
        product.deactivate();

        IllegalStateException targetEx = assertThrows(IllegalStateException.class,
                () -> product.deactivate());

        assertEquals("Product is already deactivated", targetEx.getMessage());
    }

    @Test
    @DisplayName("Should Activate Product And Register ProductUpdated Event")
    void shouldActivateProductAndRegisterProductUpdatedEvent() {
        ProductRoot product = createValidProduct();
        product.deactivate();
        product.clearDomainEvents();

        assertFalse(product.isActive());

        product.activate();

        assertTrue(product.isActive());

        // Verify event registration
        assertEquals(1, product.getDomainEvents().size());
        assertTrue(product.getDomainEvents().get(0) instanceof ProductUpdated);
    }

    @Test
    @DisplayName("Should Throw IllegalStateException When Activating Already Active Product")
    void shouldThrowIllegalStateExceptionWhenActivatingAlreadyActiveProduct() {
        ProductRoot product = createValidProduct();

        IllegalStateException targetEx = assertThrows(IllegalStateException.class,
                () -> product.activate());

        assertEquals("Product is already active", targetEx.getMessage());
    }

    @Test
    @DisplayName("Should Return True When Product Has Available Stock And Is Active")
    void shouldReturnTrueWhenProductHasAvailableStockAndIsActive() {
        ProductRoot product = createProductWithStock(100);

        assertTrue(product.hasAvailableStock(50));
        assertTrue(product.hasAvailableStock(100));
        assertTrue(product.hasAvailableStock(1));
    }

    @Test
    @DisplayName("Should Return False When Product Does Not Have Available Stock")
    void shouldReturnFalseWhenProductDoesNotHaveAvailableStock() {
        ProductRoot product = createProductWithStock(50);

        assertFalse(product.hasAvailableStock(51));
        assertFalse(product.hasAvailableStock(100));
    }

    @Test
    @DisplayName("Should Return False When Product Is Inactive Even With Stock")
    void shouldReturnFalseWhenProductIsInactiveEvenWithStock() {
        ProductRoot product = createProductWithStock(100);
        product.deactivate();

        assertFalse(product.hasAvailableStock(10));
    }

    @Test
    @DisplayName("Should Support Equals And HashCode By ID")
    void shouldSupportEqualsAndHashCodeByID() {
        ProductRoot product1 = createValidProduct();
        ProductRoot product2 = createValidProduct();

        // Different products should not be equal
        assertNotEquals(product1, product2);
        assertNotEquals(product1.getId(), product2.getId());
    }

    @Test
    @DisplayName("Should Have A Non Null ToString")
    void shouldHaveANonNullToString() {
        ProductRoot product = createValidProduct();

        assertNotNull(product.toString());
        assertFalse(product.toString().isEmpty());
    }

    private ProductRoot createValidProduct() {
        return ProductRoot.create(
                SKU.of("LAPTOP-001"),
                ProductName.of("Laptop Computer"),
                "High-performance laptop",
                Money.of(999.99, USD),
                Stock.of(100),
                CategoryReference.of("cat-electronics"),
                ProductImage.of("https://example.com/laptop.jpg"),
                "test-user"
        );
    }

    private ProductRoot createProductWithStock(int stockAmount) {
        return ProductRoot.create(
                SKU.of("MOUSE-001"),
                ProductName.of("Wireless Mouse"),
                "Ergonomic wireless mouse",
                Money.of(29.99, USD),
                Stock.of(stockAmount),
                CategoryReference.of("cat-electronics"),
                ProductImage.of("https://example.com/mouse.jpg"),
                "test-user"
        );
    }
}
