package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import product.Product;
import product.ProductDao;

class ShoppingServiceTest {

    private ProductDao productDao = Mockito.mock(ProductDao.class);

    private ShoppingService shoppingService = new ShoppingServiceImpl(productDao);

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(1L, "89008008080");
    }

    /**
     * Тест на получение корзины, если пользователь существует и что она 0 при создании
     */
    @Test
    void getCart() {
        Cart cart = shoppingService.getCart(customer);
        Assertions.assertNotNull(cart);
        Assertions.assertEquals(0, cart.getProducts().size());
    }

    /**
     * Тест на взятие корзины, если пользователь не существует
     */
    @Test
    void getCartIfCustomerIsNull() {
        Cart cart = shoppingService.getCart(null);
        Assertions.assertNotNull(cart);
        Assertions.assertEquals(0, cart.getProducts().size());
    }

    /**
     * Тест, что мы запрашиваем все продукты
     */
    @Test
    void getAllProducts() {
        shoppingService.getAllProducts();

        Mockito.verify(productDao).getAll();
    }

    /**
     * Тест, что мы получили все продукты по имени
     */
    @Test
    void getProductByName() {
        shoppingService.getProductByName("Sam");

        Mockito.verify(productDao).getByName(Mockito.anyString());
    }

    /**
     * Тест, что мы можем совершить покупку, если корзина не пустая
     * и она существует, и товар доступен для покупки, и количество товара уменьшается при покупке
     */
    @Test
    void buy() {
        Cart cart = new Cart(null);
        Product bread = new Product("bread", 2);
        cart.add(bread, 1);
        cart.add(new Product("Snickers", 4), 3);

        Assertions.assertDoesNotThrow(() ->
        {
            Assertions.assertTrue(shoppingService.buy(cart));
            Mockito.verify(productDao, Mockito.times(2)).save(Mockito.any());
            Assertions.assertEquals(1, cart.getProducts().get(bread));
        });
    }

    /**
     * Тест, что остаток продуктов равен количеству, которое мы хотим взять и мы совершаем покупку(тест упадет!)
     * Проблема не в тестируемом методе, а другом
     */
    @Test
    void buyWhenProductCountEqualsLastCount() {
        Cart cart = new Cart(null);
        cart.add(new Product("bread", 1), 1);

        Assertions.assertDoesNotThrow(() ->
        {
            Assertions.assertTrue(shoppingService.buy(cart));
            Mockito.verify(productDao).save(Mockito.any());
        });
    }

    /**
     * Тест, что нет владельца корзина и мы совершаем покупку, тут логическая ошибка (тест упадет!)
     */
    @Test
    void buyWhenCustomerIsEmpty() {
        Cart cart = new Cart(null);
        cart.add(new Product("bread", 2), 1);

        Assertions.assertThrows(BuyException.class, () ->
        {
            Assertions.assertFalse(shoppingService.buy(cart));
            Mockito.verify(productDao, Mockito.never()).save(Mockito.any());
            Assertions.assertEquals(1, cart.getProducts().size());
        });
    }

    /**
     * Тест, что мы не совершаем покупку, если корзина пустая
     */
    @Test
    void buyIfCartEmpty() {
        Cart cart = new Cart(customer);
        Assertions.assertEquals(0, cart.getProducts().size());

        Assertions.assertDoesNotThrow(() ->
        {
            Assertions.assertFalse(shoppingService.buy(cart));
            Mockito.verify(productDao, Mockito.never()).save(Mockito.any());
        });
    }

    /**
     * Тест, что мы не совершаем покупку, если товары null
     */
    @Test
    void buyIfGoodsNull() {
        Cart cart = new Cart(customer);
        cart.add(new Product(null, 100), 1);

        Assertions.assertThrows(BuyException.class, () ->
        {
            Assertions.assertFalse(shoppingService.buy(cart));
            Mockito.verify(productDao).save(Mockito.any());
            Assertions.assertEquals(1, cart.getProducts().size());
        });
    }

    /**
     * Тест, что мы не можем совершить покупку, если количество товара 0 (Тест упадет!)
     */
    @Test
    void buyIfCount0() {
        Cart cart = new Cart(customer);
        cart.add(new Product("Bread", 100), 0);

        Assertions.assertThrows(BuyException.class, () ->
        {
            Assertions.assertFalse(shoppingService.buy(cart));
            Mockito.verify(productDao, Mockito.never()).save(Mockito.any());
            Assertions.assertEquals(1, cart.getProducts().size());
        });
    }
}
