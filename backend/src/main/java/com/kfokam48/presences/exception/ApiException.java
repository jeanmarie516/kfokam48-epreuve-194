package com.kfokam48.presences.exception;

import org.springframework.http.HttpStatus;

/**
 * Toute erreur métier passe par cette exception : le gestionnaire centralisé
 * la traduit au format imposé { code, message } — jamais de stack trace (B4).
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    // ---- Codes du contrat d'API ----

    public static ApiException champManquant(String champ) {
        return new ApiException(HttpStatus.BAD_REQUEST, "CHAMP_MANQUANT",
                "Le champ " + champ + " est obligatoire.");
    }

    public static ApiException codeInconnu() {
        return new ApiException(HttpStatus.BAD_REQUEST, "CODE_INCONNU",
                "Ce code ne correspond à aucune session ouverte.");
    }

    public static ApiException codeExpire() {
        return new ApiException(HttpStatus.GONE, "CODE_EXPIRE",
                "Le code de présence a expiré.");
    }

    public static ApiException dejaPresent() {
        return new ApiException(HttpStatus.CONFLICT, "DEJA_PRESENT",
                "Vous êtes déjà marqué présent pour cette session.");
    }

    public static ApiException etudiantBloque() {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "ETUDIANT_BLOQUE",
                "Trop de codes erronés, réessayez dans 2 minutes.");
    }

    public static ApiException lienInvalide() {
        return new ApiException(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE",
                "Le lien doit être une URL http(s) valide.");
    }

    public static ApiException exerciceDejaDepose() {
        return new ApiException(HttpStatus.CONFLICT, "EXERCICE_DEJA_DEPOSE",
                "Vous avez déjà déposé un exercice pour cette session.");
    }

    public static ApiException lienVerrouille() {
        return new ApiException(HttpStatus.CONFLICT, "LIEN_VERROUILLE",
                "Le lien ne peut plus être modifié, l'exercice a été relu.");
    }

    public static ApiException noteInvalide() {
        return new ApiException(HttpStatus.BAD_REQUEST, "NOTE_INVALIDE",
                "La note doit être un entier entre 0 et 20.");
    }

    public static ApiException autoRelectureInterdite() {
        return new ApiException(HttpStatus.FORBIDDEN, "AUTO_RELECTURE_INTERDITE",
                "On ne peut pas relire son propre exercice.");
    }

    public static ApiException relectureDejaRendue() {
        return new ApiException(HttpStatus.CONFLICT, "RELECTURE_DEJA_RENDUE",
                "Cette relecture a déjà été rendue.");
    }

    public static ApiException sessionCloturee() {
        return new ApiException(HttpStatus.CONFLICT, "SESSION_CLOTUREE",
                "La session est clôturée.");
    }

    public static ApiException sessionInconnue() {
        return new ApiException(HttpStatus.NOT_FOUND, "SESSION_INCONNUE",
                "Cette session n'existe pas.");
    }

    public static ApiException promotionInconnue() {
        return new ApiException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE",
                "Cette promotion n'existe pas.");
    }

    public static ApiException etudiantInconnu() {
        return new ApiException(HttpStatus.NOT_FOUND, "ETUDIANT_INCONNU",
                "Cet étudiant n'existe pas.");
    }

    public static ApiException exerciceInconnu() {
        return new ApiException(HttpStatus.NOT_FOUND, "EXERCICE_INCONNU",
                "Cet exercice n'existe pas.");
    }

    public static ApiException relectureInconnue() {
        return new ApiException(HttpStatus.NOT_FOUND, "RELECTURE_INCONNUE",
                "Cette relecture n'existe pas.");
    }
}
