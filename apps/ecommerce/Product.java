import java.util.HashMap;

public class Product {

    private int id;
    private String barcode;
    private String name;
    private String category;
    private float priceWithoutTax;
    private float taxRate;
    private float discount;
    private float weight;
    private HashMap<String, String> characteristics;

    public Product(int id, String barcode, String name, String category, float priceWithoutTax, float taxRate,
            float discount, float weight) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.category = category;
        this.priceWithoutTax = priceWithoutTax;
        this.taxRate = taxRate;
        this.discount = discount;
        this.weight = weight;
        characteristics = new HashMap<String, String>();
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * @param barcode the barcode to set
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the priceWithoutTax
     */
    public float getPriceWithoutTax() {
        return priceWithoutTax;
    }

    /**
     * @param priceWithoutTax the priceWithoutTax to set
     */
    public void setPriceWithoutTax(float priceWithoutTax) {
        this.priceWithoutTax = priceWithoutTax;
    }

    /**
     * @return the taxRate
     */
    public float getTaxRate() {
        return taxRate;
    }

    /**
     * @param tax the taxRate to set
     */
    public void setTax(float taxRate) {
        this.taxRate = taxRate;
    }

    /**
     * @return the discount
     */
    public float getDiscount() {
        return discount;
    }

    /**
     * @param discount the discount to set
     */
    public void setDiscount(float discount) {
        this.discount = discount;
    }

    /**
     * @return the weight
     */
    public float getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * @return the characteristics
     */
    public HashMap<String, String> getCharacteristics() {
        return characteristics;
    }

    /**
     * @param characteristic the characteristic to add
     * @param value the value of the characteristic
     */
    public void addCharacteristic(String characteristic, String value) {
        characteristics.put(characteristic, value);
    }

}