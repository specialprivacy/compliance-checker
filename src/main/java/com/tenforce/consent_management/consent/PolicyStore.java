package com.tenforce.consent_management.consent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nustaq.serialization.FSTConfiguration;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class PolicyStore {
    private static volatile PolicyStore INSTANCE;
    private static final FSTConfiguration serializer = FSTConfiguration.createDefaultConfiguration();
    private final RocksDB db;

    private PolicyStore() throws RocksDBException {
        if (INSTANCE != null) throw new IllegalStateException("Singleton already initialized");
        RocksDB.loadLibrary();
        db = RocksDB.open("/tmp/rocks-policy-store");
    }

    @NotNull
    public static PolicyStore getInstance() throws RocksDBException {
       if (INSTANCE != null) return INSTANCE;
       synchronized (PolicyStore.class) {
           if (INSTANCE == null) {
               INSTANCE = new PolicyStore();
           }
       }
       return INSTANCE;
    }

    public void updatePolicy(@NotNull String id, @NotNull OWLClassExpression policy) throws RocksDBException {
        db.put(id.getBytes(), serializer.asByteArray(policy));
    }

    @Nullable
    public OWLClassExpression getPolicy(@NotNull String id) throws RocksDBException {
        // TODO: maybe try to reuse buf for better performance
        byte[] buf = db.get(id.getBytes());
        if (buf == null) return null;
        return (OWLClassExpression) serializer.asObject(buf);
    }
}
