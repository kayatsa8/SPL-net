package bgu.spl.net.srv.Messages;

import java.util.ArrayList;
import java.util.List;

public class PostMessage extends Message{

    private String content;

    public PostMessage(short _opcode, String _content) {
        super((short)5);
        content = _content;
    }

    public String getContent(){
        return content;
    }

    public List<String> getTagged(){
        List<String> tagged = new ArrayList<>();
        String tag = "";

        for(int i=0; i<content.length(); i++){
            if (content.charAt(i) == '@') {
                i++;
                while(i<content.length() && content.charAt(i) != ' '){
                    tag += content.charAt(i);
                    i++;
                }
                tagged.add(tag);
                tag = "";
            }
        }

        return tagged;
    }
}
