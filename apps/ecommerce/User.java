public class User{
    private int id;
    private String login;
	private String fullName;
	private String address;
    
    public User(int id, String login, String fullName, String address){
        this.id = id;
        this.login = login;
        this.fullName = fullName;
        this.address = address;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }
}