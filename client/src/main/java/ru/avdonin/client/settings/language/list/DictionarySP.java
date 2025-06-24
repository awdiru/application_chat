package ru.avdonin.client.settings.language.list;

import lombok.Getter;
import ru.avdonin.client.settings.language.BaseDictionary;

@Getter
public class DictionarySP extends BaseDictionary {
    private final String locale = "SP";
    private final String customization = "Español";
    private final String yes = "Sí";
    private final String no = "No";
    private final String confirmation = "Confirmación";
    //Ventana de autorización
    private final String authorization = "Autorización";
    private final String login = "Iniciar sesión";
    private final String signup = "Registrarse";
    private final String username = "Nombre de usuario";
    private final String password = "Contraseña";
    //Ventana de chat
    private final String chat = "Chat";
    private final String chats = "Chats";
    //Ventana de chats
    private final String addChatTitle = "Crear chat";
    private final String chatName = "Nombre del chat";
    //Menú contextual
    private final String logoutChat = "Salir del chat";
    private final String logoutChatQuestion = "¿Estás seguro de que quieres salir del chat?";
    private final String renameChatCustom = "Renombrar chat (solo para ti)";
    private final String renameChatAdmin = "Renombrar chat (para todos)";
    private final String rename = "Renombrar";
    private final String addUser = "Añadir usuario";
    private final String addUserQuestion = "Nombre de usuario";
    private final String add = "Añadir";
    //Ventana de configuración
    private final String settingsTitle = "Configuración";
    private final String settingsLanguage = "Idioma";
    private final String settingsTimeZone = "Zona horaria";
    private final String settingsSystem = "Sistema";
    private final String restartProgram = "Para aplicar los cambios, es necesario reiniciar el programa.";
    private final String warning = "Advertencia";
    //Mensajes de error
    private final String error = "Error";
    private final String authorizationError = "Error de autorización";
    private final String errorCode = "Código de error";
    private final String statusCode = "Código de estado";
    //Meses (en forma base)
    private final String january = "enero";
    private final String february = "febrero";
    private final String march = "marzo";
    private final String april = "abril";
    private final String may = "mayo";
    private final String june = "junio";
    private final String july = "julio";
    private final String august = "agosto";
    private final String september = "septiembre";
    private final String october = "octubre";
    private final String november = "noviembre";
    private final String december = "diciembre";
    //Días de la semana
    private final String monday = "Lun";
    private final String tuesday = "Mar";
    private final String wednesday = "Mié";
    private final String thursday = "Jue";
    private final String friday = "Vie";
    private final String saturday = "Sáb";
    private final String sunday = "Dom";
}
