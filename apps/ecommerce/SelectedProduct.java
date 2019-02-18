public class SelectedProduct {

    private Product product;
    private int selectedQuantity;

    public SelectedProduct(Product product, int selectedQuantity) {
        this.product = product;
        this.selectedQuantity = selectedQuantity;
    }

    public boolean equals(Object o) {
        if (o == null || product == null || !(o instanceof SelectedProduct)
                || ((SelectedProduct) o).getProduct() == null)
            return false;
        
        return product.getId() == ((SelectedProduct) o).getProduct().getId();
    }

    /**
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @return the selectedQuantity
     */
    public int getSelectedQuantity() {
        return selectedQuantity;
    }

    /**
     * @param selectedQuantity the selectedQuantity to set
     */
    public void addQuantity(int quantity) {
        this.selectedQuantity += quantity;
    }

}