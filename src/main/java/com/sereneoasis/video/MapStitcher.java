package com.sereneoasis.video;

import com.sereneoasis.SereneVideo;
import com.sereneoasis.utils.PacketUtils;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftItemFrame;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.map.CraftMapView;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapStitcher {


    private BufferedImage displayImage;

    private int xBlocks, yBlocks;
    
    private static final int PIXEL = 128;


    private HashMap<Pair<Integer, Integer>, ItemFrame> images = new HashMap<>();

    private World world;

    private BufferedImage cloneOriginal(){
        BufferedImage copyOfImage = new BufferedImage(displayImage.getWidth(), displayImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = copyOfImage.createGraphics();
        g.drawImage(displayImage, 0, 0, null);
        return copyOfImage; //or use it however you want
    }

    public MapStitcher(Location loc, BufferedImage img){

            displayImage = img;

             xBlocks = Math.floorDiv(displayImage.getWidth(), PIXEL)  ;
             yBlocks = Math.floorDiv(displayImage.getHeight(), PIXEL) ;
            this.world = loc.getWorld();

            loc.add(xBlocks/2, yBlocks, 0);

            loc.getWorld().getNearbyEntities(loc, xBlocks, yBlocks, xBlocks)
                    .forEach(entity -> {
                        if (entity instanceof ItemFrame){
                            entity.remove();
                        }
                    });

            for (int x = 0; x < xBlocks; x+=1) {

                for (int y = 0; y < yBlocks; y += 1) {

                    ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                    MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
                    mapMeta.setMapView(Bukkit.createMap(loc.getWorld()));

                    BufferedImage image = displayImage.getSubimage(x * PIXEL, y * PIXEL, PIXEL, PIXEL);


                    mapMeta.getMapView().addRenderer(new MapRenderMan(image, PIXEL));
                    mapItem.setItemMeta(mapMeta);

//                        player.getInventory().addItem(mapItem);
//                        }

                    loc.clone().add(0,0,1).getBlock().setType(Material.DIRT);

                    int finalX = x;
                    int finalY = y;
                    loc.getWorld().spawn(loc, EntityType.ITEM_FRAME.getEntityClass(), (entity) -> {
                        ((ItemFrame) entity).setItem(mapItem);
                        images.put(new Pair<>(finalX, finalY), ((ItemFrame) entity));
                    });


                    loc.add(0,-1,0);
                }
                loc.add(0,yBlocks,0);
                loc.add(-1,0,0);
            }
//            player.sendMessage(ChatColor.GREEN + "Here you go!");

    }

    public static byte[] imageToBytes(@NotNull BufferedImage image) {
//        BufferedImage temp = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//        Graphics2D graphics = temp.createGraphics();
//        graphics.drawImage(image, 0, 0, null);
//        graphics.dispose();

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        byte[] result = new byte[image.getWidth() * image.getHeight()];
        for (int i = 0; i < pixels.length; i++) {
            result[i] = MapPalette.matchColor(new Color(pixels[i], false));
        }
        return result;
    }


    public void changeImages(BufferedImage img){

        for (int x = 0; x < xBlocks; x+=1) {

            for (int y = 0; y < yBlocks; y += 1) {

//                        if (displayImage.getWidth() > xBlocks*PIXEL + PIXEL  && displayImage.getHeight() > yBlocks*PIXEL + PIXEL) {
                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
                mapMeta.setMapView(Bukkit.createMap(world));
//                        BufferedImage image = cloneOriginal().getSubimage(x * PIXEL, y * PIXEL, PIXEL, PIXEL);
                BufferedImage image = img.getSubimage(x * PIXEL, y * PIXEL, PIXEL, PIXEL);
                mapMeta.getMapView().addRenderer(new MapRenderMan(image, PIXEL));
                mapItem.setItemMeta(mapMeta);

//                        player.getInventory().addItem(mapItem);
//                        }

                int finalY = y;
                int finalX = x;

                Pair<Integer, Integer> key = images.keySet().stream().filter(integerIntegerPair -> integerIntegerPair.getA() == finalX && integerIntegerPair.getB() == finalY).findAny().get();

                ItemFrame itemFrame = images.get(key);



//                itemFrame.setItem(null);

                CraftItemFrame craftItemFrame =  (CraftItemFrame) images.get(key);

                net.minecraft.world.entity.decoration.ItemFrame nmsItemFrame = craftItemFrame.getHandle();

                net.minecraft.world.item.ItemStack nmsMap = CraftItemStack.asNMSCopy(mapItem);

//                nmsItemFrame.setItem(nmsMap);

                List<SynchedEntityData.DataValue<?>> metadata = new ArrayList<>();
//                List<SynchedEntityData.DataValue<?>> metadata = nmsItemFrame.getEntityData().getNonDefaultValues();

                Bukkit.getScheduler().runTaskAsynchronously(SereneVideo.plugin, () -> {
                    byte[] pixels = imageToBytes(image);
                    MapItemSavedData.MapPatch updateData = new MapItemSavedData.MapPatch(0, 0, 128, 128, pixels);
                    ClientboundMapItemDataPacket clientboundMapItemDataPacket = new ClientboundMapItemDataPacket(MapItem.getMapId(nmsMap),  (byte) 4, false, null, updateData  );

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        PacketUtils.sendPacket(clientboundMapItemDataPacket, player);

                    });

                    metadata.add(new SynchedEntityData.DataValue<>(8, EntityDataSerializers.ITEM_STACK, nmsMap));

                    ClientboundSetEntityDataPacket clientboundSetEntityDataPacket = new ClientboundSetEntityDataPacket(craftItemFrame.getEntityId(), metadata);

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        PacketUtils.sendPacket(clientboundSetEntityDataPacket, player);

                    });
                });


                //                images.get(key).setItem(mapItem);
            }
        }
    }

    public MapStitcher(Player player, String url){
        try {

            displayImage = ImageIO.read(new URL(url));

            int xBlocks = Math.floorDiv(displayImage.getWidth(), PIXEL)  ;
            int yBlocks = Math.floorDiv(displayImage.getHeight(), PIXEL) ;


            Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(3)).add(0,yBlocks,0);

            for (int x = 0; x < xBlocks; x+=1) {

                for (int y = 0; y < yBlocks; y += 1) {

//                        if (displayImage.getWidth() > xBlocks*PIXEL + PIXEL  && displayImage.getHeight() > yBlocks*PIXEL + PIXEL) {
                    ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                    MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
                    mapMeta.setMapView(Bukkit.createMap(player.getWorld()));
//                        BufferedImage image = cloneOriginal().getSubimage(x * PIXEL, y * PIXEL, PIXEL, PIXEL);
                    BufferedImage image = displayImage.getSubimage(x * PIXEL, y * PIXEL, PIXEL, PIXEL);
                    mapMeta.getMapView().addRenderer(new MapRenderMan(image, PIXEL));
                    mapItem.setItemMeta(mapMeta);

//                        player.getInventory().addItem(mapItem);
//                        }
                    loc.clone().add(0,0,1).getBlock().setType(Material.DIRT);
                    player.getWorld().spawn(loc, EntityType.ITEM_FRAME.getEntityClass(), (entity) -> {
                        ((ItemFrame) entity).setItem(mapItem);
                    });


                    loc.add(0,-1,0);
                }
                loc.add(0,yBlocks,0);
                loc.add(-1,0,0);
            }



            player.sendMessage(ChatColor.GREEN + "Here you go!");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MapStitcher(Player player){
            try {

                displayImage = ImageIO.read(new URL("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Instagram_icon.png/2048px-Instagram_icon.png"));

                int xBlocks = Math.floorDiv(displayImage.getWidth(), PIXEL)  ;
                int yBlocks = Math.floorDiv(displayImage.getHeight(), PIXEL) ;


                Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(3)).add(0,yBlocks,0);

                for (int x = 0; x < xBlocks; x+=1) {

                    for (int y = 0; y < yBlocks; y += 1) {

//                        if (displayImage.getWidth() > xBlocks*PIXEL + PIXEL  && displayImage.getHeight() > yBlocks*PIXEL + PIXEL) {
                        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                        MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
                        mapMeta.setMapView(Bukkit.createMap(player.getWorld()));
//                        BufferedImage image = cloneOriginal().getSubimage(x * PIXEL, y * PIXEL, PIXEL, PIXEL);
                        BufferedImage image = displayImage.getSubimage(x * PIXEL, y * PIXEL, PIXEL, PIXEL);
                        mapMeta.getMapView().addRenderer(new MapRenderMan(image, PIXEL));
                        mapItem.setItemMeta(mapMeta);

//                        player.getInventory().addItem(mapItem);
//                        }
                        loc.clone().add(0,0,1).getBlock().setType(Material.DIRT);
                        player.getWorld().spawn(loc, EntityType.ITEM_FRAME.getEntityClass(), (entity) -> {
                            ((ItemFrame) entity).setItem(mapItem);
                        });


                        loc.add(0,-1,0);
                    }
                    loc.add(0,yBlocks,0);
                    loc.add(-1,0,0);
                }



                player.sendMessage(ChatColor.GREEN + "Here you go!");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
