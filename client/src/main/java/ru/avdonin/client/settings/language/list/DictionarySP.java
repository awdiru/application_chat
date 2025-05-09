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
    private final String friends = "Amigos";
    private final String requestFriends = "Solicitudes de amistad";
    //Статус бар
    private final String changeUser = "Cambiar usuario";
    private final String restart = "Volver a cargar";
    //Окно друзей
    private final String addFriendTitle = "Añadir un amigo";
    private final String rmFriendTitle = "Eliminar amigo";
    private final String friendName = "Nombre del amigo";
    //Окно принятия запроса в друзья
    private final String confirmFriendTitle = "Confirmación de la solicitud de amistad";
    private final String confirmFriend = "Aceptar solicitud";
    private final String rejectedFriend = "Rechazar solicitud";
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
