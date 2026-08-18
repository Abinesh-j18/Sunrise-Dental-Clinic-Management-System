package model;

/**
 * Model representing a dashboard navigation item or action.
 * Returned polymorphically by User subclasses to build role-specific menus.
 *
 * @author Student
 */
public class DashboardMenuItem {
    private String id;
    private String title;
    private String description;
    private String iconKey;
    private String category;

    public DashboardMenuItem() {
    }

    public DashboardMenuItem(String id, String title, String description, String iconKey, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconKey = iconKey;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return title;
    }
}
