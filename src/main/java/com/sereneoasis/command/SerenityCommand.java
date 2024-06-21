package com.sereneoasis.command;

import com.sereneoasis.video.VideoFrameGrabber;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bytedeco.javacv.FrameGrabber;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SerenityCommand implements CommandExecutor {

//    private static final Set<ChatMaster> chatMasters = new HashSet<>();
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
//        if (commandSender instanceof Player player) {
        Player player = Bukkit.getPlayer("Sakrajin");
            if (strings.length == 0) {
                
            }
            switch (strings[0]) {

                case "video" -> {
                    if (strings.length == 1){
                        return false;
                    }
                    if (strings[1].equals("browse")){
                        if (strings.length == 2){
                            return false;
                        }
                        try {
                            Document mainDoc = Jsoup.connect(strings[2]).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36").get();

//                            Elements allLinks = new Elements();

                            List<String> allLinks = new ArrayList<>();
                            mainDoc.select("video").forEach(element -> {
                             allLinks.add(element.attr("data-mp4").toString());
                            });
                            mainDoc.select("video").forEach(element -> {
                                allLinks.add(element.attr("src").toString());
                            });

                            mainDoc.select("source").forEach(element -> {
                                allLinks.add(element.attr("src").toString());
                            });

                            mainDoc.select("meta").forEach(element -> {
                                if (element.attr("property").equals("og:image")) {
                                    String mediaLink = element.attr("content").toString();
                                    if (mediaLink.contains("gif")){
                                        allLinks.add(mediaLink);
                                    }


                                }
                            });

//                            for (String link : allLinks) {
                            String link = allLinks.get(new Random().nextInt(allLinks.size()));
                                if (link.contains("https://")) {
                                    Bukkit.broadcastMessage(link);

                                    new VideoFrameGrabber(link);
                                    break;
//                                }
                            }
                        } catch (IOException e) {
//                            throw new RuntimeException(e);
                        } catch (FrameGrabber.Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else {
                        try {
                            new VideoFrameGrabber(strings[1]);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        } catch (FrameGrabber.Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            }
            
            

        }
        return true;
    }
}