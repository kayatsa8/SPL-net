package bgu.spl.net.srv.MainPackage;

import bgu.spl.net.api.bidi.BidiMessagingProtocolIMPL;
import bgu.spl.net.api.bidi.MessageEncoderDecoderIMPL;
import bgu.spl.net.srv.Server;

public class TPCMain {

    public static void main(String[] args){

        Server.threadPerClient(
                7777, //port
                () -> new BidiMessagingProtocolIMPL(), //protocol factory
                MessageEncoderDecoderIMPL::new //message encoder decoder factory
        ).serve();
    }

}
