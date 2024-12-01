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
     * Тест на получение корзины, если пользователь существует и что она 0 при создании.
     * И это корзина того же самого пользователя
     */
    @Test
    void getCart() {
        Cart cart = shoppingService.getCart(customer);
        Product product = new Product("bread", 20);
        cart.add(product, 1);
        Assertions.assertNotNull(cart);
        Assertions.assertEquals(1, cart.getProducts().size());
        Assertions.assertEquals(1, shoppingService.getCart(customer).getProducts().size());
    }

    /**
     * Тест на взятие корзины, если пользователь не существует и это та же корзина
     */
    @Test
    void getCartIfCustomerIsNull() {
        Cart cart = shoppingService.getCart(null);
        Product product = new Product("bread", 20);
        cart.add(product, 1);
        Assertions.assertNotNull(cart);
        Assertions.assertEquals(1, cart.getProducts().size());
        Assertions.assertEquals(1, shoppingService.getCart(null).getProducts().size());
    }

    /**
     * Тест, что мы запрашиваем все продукты,
     * Нет необходимости в тестировании все и так очевидно
     */
    @Test
    void getAllProducts() {
    }

    /**
     * Тест, что мы получили все продукты по имени,
     * нет необходимости тестировать, все и так очевидно
     */
    @Test
    void getProductByName() {
    }


    /**
     * Тест, что мы можем совершить покупку, если корзина не пустая
     * и она существует, и товар доступен для покупки, и количество товара уменьшается при покупке.
     * Корзина очищается после покупки. Тест упадет.
     */
    @Test
    void buy() {
        Cart cart = new Cart(customer);
        Product bread = new Product("bread", 2);
        cart.add(bread, 1);
        Product snickers = new Product("Snickers", 4);
        cart.add(snickers, 3);

        Assertions.assertDoesNotThrow(() ->
        {
            Assertions.assertTrue(shoppingService.buy(cart));
            Mockito.verify(productDao, Mockito.times(2)).save(Mockito.any());
            Assertions.assertEquals(1, cart.getProducts().get(bread));
            Assertions.assertEquals(3, cart.getProducts().get(bread));
        });
    }

    /**
     * Тест, что остаток продуктов равен количеству, которое мы хотим взять и мы совершаем покупку(тест упадет!)
     * Проблема не в тестируемом методе, а в другом
     */
    @Test
    void buyWhenProductCountEqualsLastCount() {
        Cart cart = new Cart(null);
        cart.add(new Product("bread", 1), 1);

        Assertions.assertDoesNotThrow(() ->
        {
            Assertions.assertTrue(shoppingService.buy(cart));
            Mockito.verify(productDao).save(Mockito.any());
            Assertions.assertEquals(0, cart.getProducts().size());
        });
    }

    /**
     * Тест, что нет владельца корзина и мы совершаем покупку, тут логическая ошибка (тест упадет!)
     */
    @Test
    void buyWhenCustomerIsEmpty() {
        Cart cart = new Cart(null);
        cart.add(new Product("bread", 2), 1);

        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertFalse(shoppingService.buy(cart));
            Mockito.verify(productDao, Mockito.never()).save(Mockito.any());
            Assertions.assertEquals(1, cart.getProducts().size());
        });


    }

    /**
     * Тест, что совершаем покупку, но ожидаем false, если корзина пустая
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
     * Тест, что мы совершаем покупку, если товары null.
     * Должна быть ошибка. Ошибку придумал. Тест упадет
     */
    @Test
    void buyIfGoodsNull() {
        Cart cart = new Cart(customer);
        cart.add(new Product(null, 100), 1);

        BuyException buyException = Assertions.assertThrows(BuyException.class,
                () -> Assertions.assertFalse(shoppingService.buy(cart)));
        Assertions.assertEquals("Неверно указан товар ''",
                buyException.getMessage());

        Mockito.verify(productDao, Mockito.never()).save(Mockito.any(Product.class));
        Assertions.assertEquals(1, cart.getProducts().size());
    }

    /**
     * Тест, что мы не можем совершить покупку, если количество товара 0 (Тест упадет!)
     */
    @Test
    void buyIfCountZero() {
        Cart cart = new Cart(customer);
        cart.add(new Product("Bread", 1), 0);

        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertFalse(shoppingService.buy(cart));
            Mockito.verify(productDao, Mockito.never()).save(Mockito.any(Product.class));
            Assertions.assertEquals(1, cart.getProducts().size());
        });
    }

    /**
     *  Тест, что мы добавляем в корзину товар, но во время покупки товар уже закончился и мы его покупаем.
     */
    @Test
    void buyWhenProductInCardButBalanceNone(){
        Cart cart = new Cart(customer);
        Product bread = new Product("Bread", 2);
        cart.add(bread, 1);
        bread.subtractCount(2);

        BuyException buyException = Assertions.assertThrows(BuyException.class, () ->
                Assertions.assertFalse(shoppingService.buy(cart)));
        Assertions.assertEquals("В наличии нет необходимого количества товара 'Bread'", buyException.getMessage());
        Mockito.verify(productDao, Mockito.never()).save(Mockito.any(Product.class));
        Assertions.assertEquals(1, cart.getProducts().size());
    }
}
