import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;

public class LogHelper {
	// https://www.youtube.com/watch?v=jGrD8AZfTig
	public static JDA jda;
	public static String prefix = "-";
	
	// Main method
	public static void main(String [] args) throws LoginException {
		JDA builder = JDABuilder.createDefault("ODg2OTAwODcwNzkxNDUwNjc0.YT8UzQ.3fu38-RaboLjW9PAjkoAMhbnV-Q").build();
		builder.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
		builder.getPresence().setActivity(Activity.competing("Log Helper"));
		builder.addEventListener(new Commands());
	}
}
