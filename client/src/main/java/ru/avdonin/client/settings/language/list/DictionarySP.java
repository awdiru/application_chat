package ru.avdonin.client.settings.language.list;

import lombok.Getter;
import ru.avdonin.client.settings.language.BaseDictionary;

@Getter
public class DictionarySP extends BaseDictionary {
    private final String customization = "Español";
    private final String yes = "Sí";
    private final String no = "No";
    private final String confirmation = "Confirmación";
    //Окно авторизации
    private final String authorization = "Autorización";
    private final String login = "Iniciar sesión";
    private final String signup = "Registrarse";
    private final String username = "Nombre de usuario";
    private final String password = "Contraseña";
    //Окно чата
    private final String chat = "Chat";
    private final String chats = "Salas de chat";
    //Статус бар
    private final String changeUser = "Cambiar usuario";
    //Окно чатов
    private final String addChatTitle = "Añadir chat";
    private final String chatName = "Nombre del chat";
    //Контекстное меню
    private final String logoutChat = "La sesión de la charla";
    private final String logoutChatQuestion = "¿Estás seguro de que quieres salir del chat?";
    private final String renameChat = "Cambiar el nombre de un chat";
    private final String rename = "Cámbiale el nombre";
    //Окно настроек
    private final String settingsTitle = "Configuración";
    private final String settingsLanguage = "Idioma";
    private final String settingsTimeZone = "Zona horaria";
    private final String settingsSystem = "Sistema";
    private final String restartProgram = "Para que los cambios se apliquen, es necesario reiniciar el programa";
    private final String warning = "Advertencia";
    //Сообщения об ошибках
    private final String error = "Error";
    private final String authorizationError = "Error de autorización";
    private final String errorCode = "Código de error";
    private final String statusCode = "Estado del error";
    //Месяца
    private final String january = "Enero";
    private final String february = "Febrero";
    private final String march = "Marzo";
    private final String april = "Abril";
    private final String may = "Mayo";
    private final String june = "Junio";
    private final String july = "Julio";
    private final String august = "Agosto";
    private final String september = "Septiembre";
    private final String october = "Octubre";
    private final String november = "Noviembre";
    private final String december = "Diciembre";
    //Дни недели
    private final String monday = "Lun";
    private final String tuesday = "Mar";
    private final String wednesday = "Mié";
    private final String thursday = "Jue";
    private final String friday = "Vie";
    private final String saturday = "Sáb";
    private final String sunday = "Dom";
}
