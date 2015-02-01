package net.nashlegend.sourcewall.db;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class SWDaoGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "net.nashlegend.sourcewall.db.gen");
        addPostSubItem(schema);
        addQuestionSubItem(schema);
        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }

    private static void addPostSubItem(Schema schema) {
        Entity post = schema.addEntity("MyGroup");
        post.addIdProperty().autoincrement();
        post.addIntProperty("section").notNull();
        post.addIntProperty("type").notNull();
        post.addStringProperty("name").notNull();
        post.addStringProperty("value").notNull();
        post.addBooleanProperty("selected").notNull();
        post.addIntProperty("order").notNull();
    }

    private static void addQuestionSubItem(Schema schema) {
        Entity post = schema.addEntity("AskTag");
        post.addIdProperty().autoincrement();
        post.addIntProperty("section").notNull();
        post.addIntProperty("type").notNull();
        post.addStringProperty("name").notNull();
        post.addStringProperty("value");
        post.addBooleanProperty("selected").notNull();
        post.addIntProperty("order").notNull();
    }
}
