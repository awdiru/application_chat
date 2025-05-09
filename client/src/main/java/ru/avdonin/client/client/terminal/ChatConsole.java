package ru.avdonin.client.client.terminal;


import lombok.Getter;
import lombok.Setter;
import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.MessageListener;
import ru.avdonin.template.model.message.dto.MessageDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

@Getter
@Setter
public class ChatConsole implements MessageListener {
    private Scanner scanner = new Scanner(System.in);
    private String username;
    private String recipient;
    private Client client;

    @Override
    public void start() {
        try {
            client = new Client(this);
            login(isSignUp());
            setRecipient();
            getClient().connect(username);
            printMenu();
            printChat(client.getChat(username, recipient));

            while (true) {
                String content = scanner.nextLine();

                switch (content) {
                    case "setRecipient!":
                        setRecipient();
                        client.getChat(username, recipient);
                        break;
                    case "exit!":
                        return;
                    default:
                        client.sendMessage(content, username, recipient);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageDto messageDto) {
        System.out.println(getDate(messageDto.getTime()) + " "
                + messageDto.getSender() + ": " + messageDto.getContent());
    }

    private void printChat(List<MessageDto> messages) {
        for (MessageDto m : messages) {
            onMessageReceived(m);
        }
    }

    private void login(boolean isSignUp) throws Exception {
        String path = isSignUp ? "/signup" : "/login";

        while (true) {
            System.out.println("Введите ваше имя:");
            username = scanner.nextLine();

            System.out.println("Введите пароль:");
            String password = scanner.nextLine();
            try {
                client.login(username, password, path);
                if (path.equals("/signup")) System.out.println("Регистрация прошла успешно!");
                else System.out.println("Вход прошел успешно!");
                return;

            } catch (Exception e) {
                if (path.equals("/signup")) System.out.print("Ошибка регистрации! ");
                else System.out.print("Ошибка входа! ");
                System.out.println("Попробуйте еще раз");
            }
        }
    }

    private boolean isSignUp() {
        while (true) {
            System.out.println("Вы уже зарегистрированный пользователь (д/н)");
            String isSignUp = scanner.nextLine();
            isSignUp = isSignUp.toLowerCase();
            if (isSignUp.equals("д") || isSignUp.equals("y")) return false;
            else if (isSignUp.equals("н") || isSignUp.equals("n")) return true;
            System.out.println("Ошибка ввода, попробуйте еще раз!");
        }
    }

    private void printMenu() {
        System.out.println("Доступные команды: ");
        System.out.println("'setRecipient!' - сменить получателя");
        System.out.println("'exit!' - выйти из программы");
    }


    private void setRecipient() {
        System.out.println("Кому будете отправлять сообщения? ");
        recipient = scanner.nextLine();
    }


    private String getDate(OffsetDateTime date) {
        return getDayOfWeek(date) + ", "
                + date.getDayOfMonth() + " "
                + getMonth(date) + " "
                + date.format(DateTimeFormatter.ofPattern("hh:mm"));
    }

    private String getMonth(OffsetDateTime date) {
        return switch (date.getMonth()) {
            case JANUARY -> "января";
            case FEBRUARY -> "февраля";
            case MARCH -> "марта";
            case APRIL -> "апреля";
            case MAY -> "мая";
            case JUNE -> "июня";
            case JULY -> "июля";
            case AUGUST -> "августа";
            case SEPTEMBER -> "сентября";
            case OCTOBER -> "октября";
            case NOVEMBER -> "ноября";
            default -> "декабря";
        };
    }

    private String getDayOfWeek(OffsetDateTime date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Пн";
            case TUESDAY -> "Вт";
            case WEDNESDAY -> "Ср";
            case THURSDAY -> "Чт";
            case FRIDAY -> "Пт";
            case SATURDAY -> "Сб";
            default -> "Вс";
        };
    }
}
