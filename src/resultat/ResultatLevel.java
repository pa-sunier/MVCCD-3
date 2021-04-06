package resultat;

public enum ResultatLevel {
    FATAL,          // Une erreur fatale pour le traitement en cours
    NO_FATAL,       // Un avertissement pour le traitement en cours
    INFO;           // Une information d'état du traiment (ok, abandonné...)
}
