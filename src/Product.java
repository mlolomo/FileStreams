import java.io.Serializable;
public class Product implements Serializable {

    public static final int NAME_LEN = 35;
    public static final int DESC_LEN = 75;
    public static final int ID_LEN = 6;

    private String name;
    private String description;
    private String ID;
    private double cost;

    public Product(String name, String description, String ID, double cost) {
        this.name = name;
        this.description = description;
        this.ID = ID;
        this.cost = cost;
    }

    public Product(String name, String ID, double cost) {
        this(name, "", ID, cost);
    }

    public Product() {
        this("", "", "", 0.0);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getID() {
        return ID;
    }

    public double getCost() {
        return cost;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getFixedName() {
        return fixLength(name, NAME_LEN);
    }

    public String getFixedDescription() {
        return fixLength(description, DESC_LEN);
    }

    public String getFixedID() {
        return fixLength(ID, ID_LEN);
    }

    private String fixLength(String s, int targetLen) {
        if (s.length() > targetLen) {
            return s.substring(0, targetLen);
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < targetLen) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public String toCSV() {
        return ID + ", " + name + ", " + description + ", " + cost;
    }

    public String toJSON() {
        return "{\n" +
                "  \"ID\": \"" + ID + "\",\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"description\": \"" + description + "\",\n" +
                "  \"cost\": " + cost + "\n" +
                "}";
    }

    public String toXML() {
        return "<Product>\n" +
                "  <ID>" + ID + "</ID>\n" +
                "  <name>" + name + "</name>\n" +
                "  <description>" + description + "</description>\n" +
                "  <cost>" + cost + "</cost>\n" +
                "</Product>";
    }

    @Override
    public String toString() {
        return "Product{" +
                "ID='" + ID + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cost=" + cost +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Product product = (Product) obj;

        return Double.compare(product.cost, cost) == 0 &&
                ID.equals(product.ID) &&
                name.equals(product.name) &&
                description.equals(product.description);
    }
}