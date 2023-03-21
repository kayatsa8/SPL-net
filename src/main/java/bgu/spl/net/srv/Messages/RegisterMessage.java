package bgu.spl.net.srv.Messages;

public class RegisterMessage extends Message{

    private String username;
    private String password;
    private String birthday;

    public RegisterMessage(short _opcode, String _username, String _password, String _birthday) {
        super((short)1);
        username = _username;
        password = _password;
        birthday = _birthday;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getBirthday(){
        return birthday;
    }
}
