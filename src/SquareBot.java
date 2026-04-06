import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class SquareBot {
    public static void main(String[] args) {
        int eBucks = 0;
        int startingEbucks = 0;
        int videosWatchedPerSummary = 100;
        int winAmounts = 0;
        double winRatioPerSummary = 0;

        if (args.length > 0) eBucks = Integer.parseInt(args[0]);

        startingEbucks = eBucks;

        System.out.println("Video Games");
        System.out.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            String input;

            while ((input = br.readLine()) != null){
                input = input.trim();

                if (input.isEmpty()) continue;

                if (input.contains("=")){
                    System.err.println("New video!");
                    Map<String, String> videoAttributes = processVideoString(input);
                    int rating = calculateVideoRating(videoAttributes);

                    int startBid = 0;
                    int maxBid = 0;

                    double ebucksSpentRatio =  (double) eBucks / startingEbucks;

                    if (eBucks > 0 && rating >= 60){
                        startBid = rating;

                        double baseCoefficient = 10 + (ebucksSpentRatio * 10);
                        maxBid = (int) (rating * 20 * baseCoefficient);

                        // Be more aggressive when you have eBucks.
                        if (eBucks >= 6000000){
                            maxBid *= 2;
                        }

                        // Add aggressiveness when win ratio is low.
                        if (winRatioPerSummary <= 0.20) maxBid *= 3;

                        startBid = Math.min(startBid, eBucks);
                        maxBid = Math.min(maxBid, eBucks);

                        if (startBid > maxBid) startBid = maxBid;
                    }

                    if (maxBid > 0) {
                        System.out.println(startBid + " " + maxBid);
                    } else System.out.println("0 0");
                    System.out.flush();
                } else if (input.startsWith("W ")) {
                    String[] inputs = input.split(" ");
                    eBucks -= Integer.parseInt(inputs[1]);
                    winAmounts += 1;
                    System.err.println("Win! Bot spent: " + inputs[1] + " | Ebucks left: " + eBucks);
                } else if (input.equals("L")) {
                    System.err.println("Lost! No gain!");
                } else if (input.startsWith("S ")) {
                    String[] inputs = input.split(" ");
                    winRatioPerSummary = (double) winAmounts / videosWatchedPerSummary;
                    winAmounts = 0; // So the calculation doesn't break.
                    System.err.println("Summary. Points: " + inputs[1] + "| Ebucks spent: " + inputs[2]);
                }else{
                    System.err.println("Unknown input: " + input);
                }
            }
        } catch (Exception exception){
            System.err.println("Error: " + exception.getMessage());
        }
    }

    /**
     * Based on the read in video attributes we calculate an estimated video rating, which we will use for
     * determining the start- and maxBid.
     * Our chosen category will be 'Video Games', therefore we will pick our values that will be added to
     * the overall rating based on that.
     * Goal is to calculate a video rating in the range [0-100], which will then be used as a coefficient for the bids.
     * @param attributes Video attributes represented with a map.
     * @return Video rating.
     */
    private static int calculateVideoRating(Map<String, String> attributes) {
        int result = 0;

        // Based on some research, the demographic for video games is quite well split close to 50/50, so
        // we won't take gender into account.
        boolean subscribed = attributes.get("subscribed").equals("Y");
        long viewCount = Long.parseLong(attributes.get("viewCount"));
        String category = attributes.getOrDefault("category", "");
        String[] interests = attributes.get("interests").split(";");

        int commentCount = Integer.parseInt(attributes.get("commentCount"));

        if (category.equals("Video Games")) result += 40;

        double commentViewRatio = (double) commentCount / viewCount;
        if (commentViewRatio >= 0.05) {
            result += 10;
        } else if (commentViewRatio >= 0.01){
            result += 5;
        }

        // Might be a sleeper hit video.
        if (viewCount <= 100000) result += 5;

        if (subscribed) result += 15;

        int ageRangeValue = calculateAgeRangeValue(attributes.get("age"));
        result += ageRangeValue;

        int interestsValue = assignInterestsValue(interests);
        result += interestsValue;

        System.err.println("The final rating for the video is " + result);
        return result;
    }

    /**
     * Interests are ordered by relevance, therefore we should assign value based on that.
     * @param interests Interests, count ranging from [1-3]
     * @return Interest value which we will add to the overall rating.
     */
    private static int assignInterestsValue(String[] interests) {
        int weight = 0;

        if (interests.length >= 1 && interests[0].equals("Video Games")) weight = 20;
        if (interests.length >= 2 && interests[1].equals("Video Games")) weight = 15;
        if (interests.length >= 3 && interests[2].equals("Video Games")) weight = 10;

        return weight;
    }

    /**
     * Assign values for age ranges, which will be later added to the overall video rating.
     * @param ageRange Possible age ranges represented as strings.
     * @return Value based on the age range.
     */
    private static int calculateAgeRangeValue(String ageRange) {
        return switch (ageRange) {
            case "13-17", "18-24" -> 10;
            case "25-34", "35-44" -> 5;
            default -> 0;
        };
    }

    /**
     * Takes in video info and maps the provided features to their respective holders for better
     * processing later on when deciding start- and maxBid.
     * @param textProcessTestString video format in the form of “{field}=value(,)...\n”
     * @return A map where each key represents a unique feature about the video and its corresponding value.
     */
    private static Map<String, String> processVideoString(String textProcessTestString) {
        Map<String, String> result = new HashMap<>();

        String[] parts = textProcessTestString.split(",");

        List<String> extractedVideo = new ArrayList<>();

        // Remove unnecessary from the video text.
        for (String part : parts) {
            String videoRemoved = part.replaceAll("[a-z]+\\.", "");
            extractedVideo.add(videoRemoved);
        }

        for (String s : extractedVideo) {
            String[] feature = s.split("=");
            result.putIfAbsent(feature[0], feature[1]);
        }

        return result;
    }
}
