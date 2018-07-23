import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class Main {

	public static void main(String[] args) throws Exception {

		String[] notStocks = { "The-Dustforce-DX-5583", "The-Scrubforce-DX-6018", "The-Difficults-5462", "The-Lab-4563",
				"The-City-2800", "The-Mansion-5517", "The-Forest-2654", "tutorial0" };

		try {
			//BufferedReader in = new BufferedReader(new FileReader("records.txt"));
			//String json = in.readLine();
			//in.close();

			String json = readUrl("http://dustkid.com/json/records");
			Gson gson = new Gson();

			Scores oldScores = gson.fromJson(json, Scores.class);
			Times oldTimes = gson.fromJson(json, Times.class);

			while (true) {
				//BufferedReader inn = new BufferedReader(new FileReader("records.txt"));
				//json = inn.readLine();
				//inn.close();
				
				json = readUrl("http://dustkid.com/json/records");
				gson = new Gson();

				Scores newScores = gson.fromJson(json, Scores.class);
				Times newTimes = gson.fromJson(json, Times.class);

				Iterator<Map.Entry<String, Level>> it1 = newScores.Scores.entrySet().iterator();
				Iterator<Map.Entry<String, Level>> it2 = oldScores.Scores.entrySet().iterator();
				Iterator<Map.Entry<String, Level>> it3 = newTimes.Times.entrySet().iterator();
				Iterator<Map.Entry<String, Level>> it4 = oldTimes.Times.entrySet().iterator();

				loop: while (it1.hasNext() && it2.hasNext() && it3.hasNext() && it4.hasNext()) {
					Map.Entry<String, Level> newScore = it1.next();
					Map.Entry<String, Level> oldScore = it2.next();
					Map.Entry<String, Level> newTime = it3.next();
					Map.Entry<String, Level> oldTime = it4.next();

					for (String notStockLevel : notStocks) {
						if (newScore.getValue().getLevel().equals(notStockLevel))
							continue loop;
					}

					if (newScore.getValue().getReplay_id() != oldScore.getValue().getReplay_id()) {
						String message = message(newScore.getValue().getUser(), oldScore.getValue().getUser(),
								newScore.getValue().getLevelname(), newScore.getValue().getUsername(),
								oldScore.getValue().getUsername(), newScore.getValue().getTime(),
								oldScore.getValue().getTime(), newScore.getValue().getScore_completion(),
								oldScore.getValue().getScore_completion(), newScore.getValue().getScore_finesse(),
								oldScore.getValue().getScore_finesse(), newScore.getValue().getCharacter(), "Score");
						if (!message.isEmpty()) {
							//System.out.println(message);
							sendTweet(message);
						}
					} else if (newTime.getValue().getReplay_id() != oldTime.getValue().getReplay_id()) {
						String message = message(newTime.getValue().getUser(), oldTime.getValue().getUser(),
								newTime.getValue().getLevelname(), newTime.getValue().getUsername(),
								oldTime.getValue().getUsername(), newTime.getValue().getTime(),
								oldTime.getValue().getTime(), newTime.getValue().getScore_completion(),
								oldTime.getValue().getScore_completion(), newTime.getValue().getScore_finesse(),
								oldTime.getValue().getScore_finesse(), newTime.getValue().getCharacter(), "Time");
						if (!message.isEmpty()) {
							//System.out.println(message);
							sendTweet(message);
						}
					}
				}

				oldScores.Scores = newScores.Scores;
				oldTimes.Times = newTimes.Times;

				Thread.sleep(10000);
			}
		} catch (IOException e) {
			System.out.println("ReadUrl doesn't work");
			Thread.sleep(60000);
			main(null);
		} catch (TwitterException t) {
			t.printStackTrace();
		}
		
	}

	class Scores {
		Map<String, Level> Scores;
	}

	class Times {
		Map<String, Level> Times;
	}

	private static String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	private static String shortenUsername(String userName) {
		if (userName.length() > 60) {
			userName = userName.substring(0, 57) + "...";
		}
		return userName;
	}

	private static String properTime(double time) {
		String newTime = "";
		double remainder = time % 3600;
		int minutes = (int) remainder / 60;
		double seconds = remainder % 60;
		if (minutes != 0) {
			if (seconds < 10)
				newTime = minutes + ":0" + String.format("%.3f", seconds);
			else
				newTime = minutes + ":" + String.format("%.3f", seconds);
		} else {
			newTime = String.format("%.3f", seconds);
		}
		return newTime;
	}

	private static String message(int newUser, int oldUser, String levelname, String newUsername, String oldUsername,
			double newTime, double oldTime, int newScore_completion, int oldScore_completion, int newScore_finesse,
			int oldScore_finesse, int character, String category) {

		HashMap<Integer, Character> scoresLetters = new HashMap<Integer, Character>();
		scoresLetters.put(1, 'D');
		scoresLetters.put(2, 'C');
		scoresLetters.put(3, 'B');
		scoresLetters.put(4, 'A');
		scoresLetters.put(5, 'S');

		HashMap<Integer, String> characters = new HashMap<Integer, String>();
		characters.put(0, "Dustman");
		characters.put(1, "Dustgirl");
		characters.put(2, "Dustkid");
		characters.put(3, "Dustworth");

		String message = "";

		if ((newScore_completion + newScore_finesse) > (oldScore_completion + oldScore_finesse) && newTime == oldTime) {
			if (newUser == oldUser) {
				message = shortenUsername(newUsername) + " improved " + levelname + " (" + category
						+ ") by getting a higher score with a time of " + properTime(newTime) + ", score "
						+ scoresLetters.get(newScore_completion) + scoresLetters.get(newScore_finesse) + " as "
						+ characters.get(character) + " #Dustforce";
			} else {
				message = shortenUsername(newUsername) + " beat " + shortenUsername(oldUsername) + " on " + levelname
						+ " (" + category + ") by getting a higher score with a time of " + properTime(newTime)
						+ ", score " + scoresLetters.get(newScore_completion) + scoresLetters.get(newScore_finesse)
						+ " as " + characters.get(character) + " #Dustforce";
			}
		} else if (newTime < oldTime) {
			if (newUser == oldUser) {
				message = shortenUsername(newUsername) + " improved " + levelname + " (" + category + ") by "
						+ properTime(oldTime - newTime) + " seconds with a time of " + properTime(newTime) + ", score "
						+ scoresLetters.get(newScore_completion) + scoresLetters.get(newScore_finesse) + " as "
						+ characters.get(character) + " #Dustforce";
			} else {
				message = shortenUsername(newUsername) + " beat " + shortenUsername(oldUsername) + " on " + levelname
						+ " (" + category + ") by " + properTime(oldTime - newTime) + " seconds with a time of "
						+ properTime(newTime) + ", score " + scoresLetters.get(newScore_completion)
						+ scoresLetters.get(newScore_finesse) + " as " + characters.get(character) + " #Dustforce";
			}
		}

		return message;
	}

	class Level {
		int replay_id;
		int user;
		String level;
		String levelname;
		String username;
		double time;
		int score_completion, score_finesse;
		int character;

		public int getReplay_id() {
			return replay_id;
		}

		public int getUser() {
			return user;
		}

		public String getLevel() {
			return level;
		}

		public String getLevelname() {
			return levelname;
		}

		public String getUsername() {
			return username;
		}

		public double getTime() {
			return time / 1000;
		}

		public int getScore_completion() {
			return score_completion;
		}

		public int getScore_finesse() {
			return score_finesse;
		}

		public int getCharacter() {
			return character;
		}
	}

	private static Status sendTweet(String text) throws TwitterException {
		Twitter twitter = TwitterFactory.getSingleton();

		Status status = twitter.updateStatus(text);
		System.out.println("Successfully updated the status to [" + status.getText() + "]" + new java.util.Date());

		return status;
	}
}
