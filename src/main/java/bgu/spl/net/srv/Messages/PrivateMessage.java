package bgu.spl.net.srv.Messages;

import java.util.List;

public class PrivateMessage extends Message{

    private String username;
    private String content;
    private String date_time;

    public PrivateMessage(short _opcode, String _username, String _content, String _date_time) {
        super((short)6);
        username = _username;
        content = _content;
        date_time = _date_time;
    }

    public String getUsername(){
        return username;
    }

    public String getContent(){
        return content;
    }

    public String getDate_time(){
        return date_time;
    }

    public void censoredContent(List<String> censoredWords){
        String censoredContent = content;

        for(String s : censoredWords){
            if(censoredContent.contains(s)){
                censoredContent.replaceAll(s, "<filtered>");
            }
        }

        content = censoredContent;
    }
}
