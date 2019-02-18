public class Test{
    
    public static void main(String[] args) throws Exception{
        Checkout checkout = new Checkout();

        User[] users = new User[2];
        for(int i=0; i<2; i++){
            User user = new User(1000 + i + 1, "Login_" + (i + 1), "User_" + (i + 1), "Address_" + (i + 1));
            users[i] = user;
        }

        Product[] products = new Product[4];
        for(int i=0; i<4; i++){
            Product product = new Product(i + 1, "Barcode_" + (i + 1), "Product_" + (i + 1), "Category_" + (i + 1), 100 * (i + 1), 20, 5, 0);
            for(int j=0; j<3; j++)
                product.addCharacteristic(("characteristic" + (i + 1)) + (j + 1), ("value_" + (i + 1)) + (j + 1));
            products[i] = product;
        }

        ShoppingCart[] shoppingCarts = new ShoppingCart[2];
        for(int i=0; i<2; i++){
            shoppingCarts[i] = new ShoppingCart(users[i]);
            for(int j=0; j<3; j++)
                shoppingCarts[i].add(products[j], (i+1)*(j+1));
        }
        shoppingCarts[1].add(products[3], 8);
        
        for(int i=0; i<2; i++)
            checkout.process(shoppingCarts[i]);
    }
}