package ma.securebank.legacy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * ⚠️ Classe « héritée » volontairement dangereuse — cible de l'atelier A08-1.
 * Elle exécute une commande au moment de sa désérialisation : tout flux désérialisé
 * sans contrôle devient une exécution de code. Le correctif supprime cette classe et
 * remplace ObjectInputStream par un DTO JSON (voir le README du module A05/A08).
 */
public class ShellTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private String command;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (command != null) {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
        }
    }
}
