package com.umg.model.semantic.metadata;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MongoDatabaseMetadataService implements DatabaseMetadataService {

    private final MongoClient mongoClient;
    private final String database;

    public MongoDatabaseMetadataService(MongoClient mongoClient, String database) {
        this.mongoClient = mongoClient;
        this.database = database;
    }

    @Override
    public boolean probarConexion() {
        try {
            mongoClient.getDatabase(database).listCollectionNames().first();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean existeTabla(String databaseName, String collection) {
        String dbName = databaseName != null ? databaseName : database;
        MongoDatabase db = mongoClient.getDatabase(dbName);
        for (String name : db.listCollectionNames()) {
            if (name.equalsIgnoreCase(collection)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existeColumna(String databaseName, String collection, String field) {
        String dbName = databaseName != null ? databaseName : database;
        MongoDatabase db = mongoClient.getDatabase(dbName);
        MongoCollection<Document> col = db.getCollection(collection);

        Document sample = col.find().first();
        if (sample == null) return false;

        return fieldExistsInDocument(sample, field);
    }

    private boolean fieldExistsInDocument(Document doc, String field) {
        if (doc.containsKey(field)) return true;
        String[] parts = field.split("\\.");
        if (parts.length <= 1) return false;

        Object current = doc;
        for (String part : parts) {
            if (current instanceof Document d) {
                if (!d.containsKey(part)) return false;
                current = d.get(part);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> obtenerColumnas(String databaseName, String collection) {
        String dbName = databaseName != null ? databaseName : database;
        MongoDatabase db = mongoClient.getDatabase(dbName);
        MongoCollection<Document> col = db.getCollection(collection);

        Document sample = col.find().first();
        if (sample == null) return new ArrayList<>();

        return flattenDocumentKeys(sample, "");
    }

    private List<String> flattenDocumentKeys(Document doc, String prefix) {
        List<String> keys = new ArrayList<>();
        for (String key : doc.keySet()) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            keys.add(fullKey);
            Object value = doc.get(key);
            if (value instanceof Document nested) {
                keys.addAll(flattenDocumentKeys(nested, fullKey));
            }
        }
        return keys;
    }

    @Override
    public List<String> obtenerTablas(String databaseName) {
        String dbName = databaseName != null ? databaseName : database;
        MongoDatabase db = mongoClient.getDatabase(dbName);
        List<String> collections = new ArrayList<>();
        db.listCollectionNames().into(collections);
        return collections;
    }

    @Override
    public void cerrar() {
        mongoClient.close();
    }
}
