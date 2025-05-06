package ru.avdonin.client.settings.language.list;

import lombok.Getter;
import ru.avdonin.client.settings.language.BaseLanguage;

@Getter
public class LanguageIT extends BaseLanguage {
    private final String customization = "Italiano";
    private final String yes = "Sí";
    private final String no = "No";
    private final String confirmation = "Conferma";
    //Окно авторизации
    private final String authorization = "Autorizzazione";
    private final String login = "Accedi";
    private final String signup = "Registrati";
    private final String username = "Nome utente";
    private final String password = "Password";
    //Окно чата
    private final String chat = "Chat";
    private final String friends = "Amici";
    private final String requestFriends = "Richieste di amicizia";
    private final String changeUser = "Cambia utente";
    //Окно добавления друга
    private final String addFriendTitle = "Aggiungi amico";
    private final String friendName = "Nome dell'amico";
    //Окно принятия запроса в друзья
    private final String confirmFriendTitle = "Conferma richiesta di amicizia";
    private final String confirmFriend = "Accetta richiesta";
    private final String rejectedFriend = "Rifiuta richiesta";
    //Окно настроек
    private final String settingsTitle = "Impostazioni";
    private final String settingsLanguage = "Lingua";
    private final String restartProgram = "Per applicare le modifiche, è necessario riavviare il programma";
    private final String warning = "Avviso";
    //Сообщения об ошибках
    private final String error = "Errore";
    private final String authorizationError = "Errore di autorizzazione";
    private final String errorCode = "Codice errore";
    private final String statusCode = "Stato errore";
    //Месяца
    private final String january = "Gennaio";
    private final String february = "Febbraio";
    private final String march = "Marzo";
    private final String april = "Aprile";
    private final String may = "Maggio";
    private final String june = "Giugno";
    private final String july = "Luglio";
    private final String august = "Agosto";
    private final String september = "Settembre";
    private final String october = "Ottobre";
    private final String november = "Novembre";
    private final String december = "Dicembre";
    //Дни недели
    private final String monday = "Lun";
    private final String tuesday = "Mar";
    private final String wednesday = "Mer";
    private final String thursday = "Gio";
    private final String friday = "Ven";
    private final String saturday = "Sab";
    private final String sunday = "Dom";
}