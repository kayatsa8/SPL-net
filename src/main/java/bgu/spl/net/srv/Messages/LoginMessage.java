package bgu.spl.net.srv.Messages;

public class LoginMessage extends Message{

    private String username;
    private String password;
    private boolean captcha; // true if 1, false otherwise

    public LoginMessage(short _opcode, String _username, String _password, boolean _captcha) {
        super((short)2);
        username = _username;
        password = _password;
        captcha = _captcha;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public boolean getCaptcha(){
        return captcha;
    }

}
