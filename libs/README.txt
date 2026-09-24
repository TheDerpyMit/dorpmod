DorpMod-rebuilt libs/ drop-in folder
====================================

Any *.jar placed here is added to compile + runtime automatically
(see `implementation fileTree(dir: 'libs', ...)` in build.gradle).

Most deps already resolve via maven (curios, create slim, ponder,
registrate, flywheel, JEI, ritchiesprojectilelib, create-big-cannons
via CurseMaven, lambdynlights, sable, aaa_particles, ars_nouveau
compileOnly, mixinextras). Java sources contain NO direct imports for
create / bigcannons / vestalihy / propulsion - those are only referenced
from data/dorp/recipe/*.json and neoforge.mods.toml, so they are needed
at RUNTIME, not to compile.

Drop these in only if a maven coordinate fails OR for runtime testing:

1. vestalihy (mods.toml requires [2.5.0,)) - no public maven found.
   Download the exact 1.21.1 NeoForge jar you built 1.63 against.
2. createpropulsion (requires [1.1.0,)) - same, no public maven found.
3. immersivecomputing + littletiles (ImmersiveLaptopHelper imports
   dev.leveloper.immersivecomputing.* + team.creative.littletiles.*).
   If `dev.leveloper` / creative maven coords fail, drop jars here.
4. s_a_b companion (DistanceHelper imports dev.ryanhcode.sable.*).
   build.gradle tries maven.ryanhcode.dev first; keep the jar here as
   backup if that repo is down. Must satisfy [1.4.3,).
5. create-big-cannons backup - build.gradle uses
   curse.maven:create-big-cannons-646668:<fileId> (default 8208458 =
   v5.11.6). If CurseMaven is down, download the jar manually here and
   comment that line out.

Do NOT commit large jars to git - .gitignore does not exclude libs/,
so remove them or git-ignore selectively before pushing.
