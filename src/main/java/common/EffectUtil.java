package common;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class EffectUtil {
    public static void showRangeParticle(Player p, Location startPosition, Location pos1, Location pos2, Color fromColor, Color toColor, float particleDistance, int displayIntervalMillSeconds, int displayCount) {
        Location position1 = new Location(null,Math.min(pos1.getX(),pos2.getX()),Math.min(pos1.getY(),pos2.getY()),Math.min(pos1.getZ(),pos2.getZ()));
        Location position2 = new Location(null,Math.max(pos1.getX(),pos2.getX()),Math.max(pos1.getY(),pos2.getY()),Math.max(pos1.getZ(),pos2.getZ()));
        Location cPosition;
        if(startPosition == null){
            cPosition = position1.clone();
        }
        else{
            cPosition = startPosition;
        }
        int Ycm = (int) position2.getY() - (int) position1.getY() + 1;
        int Xcm = (int) position2.getX() - (int) position1.getX() + 1;
        int Zcm = (int) position2.getZ() - (int) position1.getZ() + 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < displayCount; i++) {
                    for (int j = 0; j < 4; j++) {
                        float Yl = 0.0f;
                        float Xl = 0.0f;
                        float Zl = 0.0f;
                        while (true) {
                            if (j == 0) {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(0, Yl, 0), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            } else if (j == 1) {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(Xcm, Yl, 0), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            } else if (j == 2) {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(0, Yl, Zcm), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            } else {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(Xcm, Yl, Zcm), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            }
                            Yl += particleDistance;
                            if (Yl > Ycm) break;
                        }
                        while (true) {
                            if (j == 0) {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(Xl, 0, 0), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            } else if (j == 1) {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(Xl, Ycm, 0), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            } else if (j == 2) {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(Xl, 0, Zcm), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            } else {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(Xl, Ycm, Zcm), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            }
                            Xl += particleDistance;
                            if (Xl > Xcm) break;
                        }
                        while (true) {
                            if (j == 0) {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(0, 0, Zl), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            } else if (j == 1) {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(Xcm, 0, Zl), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            } else if (j == 2) {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(0, Ycm, Zl), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            } else {
                                p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, cPosition.clone().add(Xcm, Ycm, Zl), 0, 0.001, 0, 0, 0, new Particle.DustTransition(fromColor, toColor, 1));
                            }
                            Zl += particleDistance;
                            if (Zl > Zcm) break;
                        }
                    }
                    try {
                        Thread.sleep(displayIntervalMillSeconds);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }).start();
    }
}
