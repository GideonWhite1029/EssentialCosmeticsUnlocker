package studio.dreamys.ecu.mixin.other.essential.cosmetics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gg.essential.connectionmanager.common.packet.cosmetic.ServerCosmeticsPopulatePacket;
import gg.essential.cosmetics.model.Cosmetic;
import gg.essential.network.connectionmanager.ConnectionManager;
import gg.essential.network.connectionmanager.handler.cosmetics.ServerCosmeticsPopulatePacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = ServerCosmeticsPopulatePacketHandler.class, remap = false)
public class MixinServerCosmeticsPopulatePacketHandler {

    private File getDumpFile() {
        String osName = System.getProperty("os.name").toLowerCase();
        File dumpFile;

        if (osName.contains("win")) {
            dumpFile = new File(new File(System.getenv("APPDATA"), "ecu"), "dump.txt");
        } else {
            dumpFile = new File(new File(System.getProperty("user.home"), "ecu"), "dump.txt");
        }

        return dumpFile;
    }

    @Inject(method = "onHandle(Lgg/essential/network/connectionmanager/ConnectionManager;Lgg/essential/connectionmanager/common/packet/cosmetic/ServerCosmeticsPopulatePacket;)V", at = @At("HEAD"))
    public void onHandle(ConnectionManager connectionManager, ServerCosmeticsPopulatePacket packet, CallbackInfo ci) {
        try {
            Gson gson = new Gson();
            List<Cosmetic> cosmetics = new ArrayList<>();
            File dumpFile = getDumpFile();
            dumpFile.getParentFile().mkdirs();

            //start with already existing or new list
            if (dumpFile.exists()) {
                String fileContent = Files.readAllLines(dumpFile.toPath()).toString();

                if (fileContent.startsWith("[")) {
                    cosmetics = gson.fromJson(fileContent, new TypeToken<List<Cosmetic>>(){}.getType());
                } else {
                    Cosmetic singleCosmetic = gson.fromJson(fileContent, Cosmetic.class);
                    cosmetics.add(singleCosmetic);
                }
            }

            //add incoming cosmetics to the list
            cosmetics.addAll(packet.getCosmetics());

            //dump the list to file
            System.out.println("[EssentialCosmeticsUnlocker] Dumping cosmetics to file...");
            PrintWriter pw = new PrintWriter(new FileOutputStream(dumpFile, false));
            pw.println(gson.toJson(cosmetics));
            pw.close();
            System.out.println("[EssentialCosmeticsUnlocker] Dumped cosmetics to file!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[EssentialCosmeticsUnlocker] Could not dump cosmetics to file.");
        }
    }
}
