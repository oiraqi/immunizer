public class Checkout {
    public void process(ShoppingCart shoppingCart) throws Exception{
        //Perform some checks, for example stock availability
        //Throw an exception if there is anything wrong
        System.out.println("Order for " + shoppingCart.getUser().getFullName());
        System.out.println("Total Price Without Tax: " + shoppingCart.getTotalPriceWithoutTax());
        System.out.println("Tax: " + shoppingCart.getTax());
        System.out.println("Total Price With Tax: " + shoppingCart.getTotalPriceWithTax());
        System.out.println();
    }
}