import java.util.HashMap;
import java.util.ArrayList;;

public class ShoppingCart {

    private User user;
    private ArrayList<SelectedProduct> selectedProducts;
    private float totalPriceWithoutTax;
    private float tax;
    private float totalWeight;

    public ShoppingCart(User user) {
        this.user = user;
        selectedProducts = new ArrayList<SelectedProduct>();
        totalPriceWithoutTax = 0;
        tax = 0;
        totalWeight = 0;
    }

    public void add(Product product, int quantity) {
        SelectedProduct selectedProduct = new SelectedProduct(product, quantity);
        int index = selectedProducts.indexOf(selectedProduct);
        if (index >= 0)
            selectedProducts.get(index).addQuantity(quantity);
        else
            selectedProducts.add(selectedProduct);

        totalPriceWithoutTax += quantity * product.getPriceWithoutTax() * (1 - product.getDiscount() / 100);
        tax += quantity * product.getTaxRate()/100 * product.getPriceWithoutTax() * (1 - product.getDiscount() / 100);
        totalWeight += product.getWeight();
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the selectedProducts
     */
    public ArrayList<SelectedProduct> getSelectedProducts() {
        return selectedProducts;
    }

    /**
     * @return the totalPriceWithoutTax
     */
    public float getTotalPriceWithoutTax() {
        return totalPriceWithoutTax;
    }

    /**
     * @return the tax
     */
    public float getTax() {
        return tax;
    }

    /**
     * @return the totalPriceWithTax
     */
    public float getTotalPriceWithTax() {
        return totalPriceWithoutTax + tax;
    }

    /**
     * @return the totalWeight
     */
    public float getTotalWeight() {
        return totalWeight;
    }
}