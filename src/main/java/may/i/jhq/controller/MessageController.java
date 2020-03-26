package may.i.jhq.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 *  消息 Controller
 *
 *  @author     jin_huaquan
 *  @date      2020/3/26 15:40
 *  @version   1.0
 */
@Controller
public class MessageController {

	@MessageMapping("/message")
	@SendTo("/topic/send")
	public String send(String message) throws Exception {
		return message;
	}
}
