package ru.avdonin.client.client.settings.language.list;

import lombok.Getter;
import ru.avdonin.client.client.settings.language.BaseDictionary;

@Getter
public class DictionaryIT extends BaseDictionary {
    private final String locale = "IT";
    private final String customization = "Italiano";
    private final String yes = "Sì";
    private final String no = "No";
    private final String confirmation = "Conferma";
    //Finestra di autorizzazione
    private final String authorization = "Autorizzazione";
    private final String login = "Accedi";
    private final String signup = "Registrati";
    private final String username = "Nome utente";
    private final String password = "Password";
    //Finestra della chat
    private final String chat = "Chat";
    private final String chats = "Chat";
    //Finestra delle chat
    private final String addChatTitle = "Crea chat";
    private final String chatName = "Nome chat";
    //Menu contestuale
    private final String logoutChat = "Esci dalla chat";
    private final String logoutChatQuestion = "Sei sicuro di voler uscire dalla chat?";
    private final String renameChatCustom = "Rinomina chat (solo per te)";
    private final String renameChatAdmin = "Rinomina chat (per tutti)";
    private final String rename = "Rinomina";
    private final String addUser = "Aggiungi utente";
    private final String addUserQuestion = "Nome utente";
    private final String add = "Aggiungi";
    //Finestra delle impostazioni
    private final String settingsTitle = "Impostazioni";
    private final String settingsLanguage = "Lingua";
    private final String settingsTimeZone = "Fuso orario";
    private final String settingsSystem = "Sistema";
    private final String restartProgram = "Per applicare le modifiche, è necessario riavviare il programma.";
    private final String warning = "Avviso";
    //Messaggi di errore
    private final String error = "Errore";
    private final String authorizationError = "Errore di autorizzazione";
    private final String errorCode = "Codice errore";
    private final String statusCode = "Codice di stato";
    //Mesi (forma base)
    private final String january = "gennaio";
    private final String february = "febbraio";
    private final String march = "marzo";
    private final String april = "aprile";
    private final String may = "maggio";
    private final String june = "giugno";
    private final String july = "luglio";
    private final String august = "agosto";
    private final String september = "settembre";
    private final String october = "ottobre";
    private final String november = "novembre";
    private final String december = "dicembre";
    //Giorni della settimana
    private final String monday = "Lun";
    private final String tuesday = "Mar";
    private final String wednesday = "Mer";
    private final String thursday = "Gio";
    private final String friday = "Ven";
    private final String saturday = "Sab";
    private final String sunday = "Dom";
}