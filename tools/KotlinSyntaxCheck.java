import java.nio.file.*;
import java.util.*;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.psi.KtPsiFactory;
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer;
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement;
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil;

/** Parses Kotlin syntax only. This is deliberately not an Android build/type check. */
public class KotlinSyntaxCheck {
  public static void main(String[] args) throws Exception {
    var disposable = Disposer.newDisposable();
    int[] counts = {0, 0};
    try {
      var environment = KotlinCoreEnvironment.createForProduction(disposable, new CompilerConfiguration(), EnvironmentConfigFiles.JVM_CONFIG_FILES);
      var factory = new KtPsiFactory(environment.getProject(), false);
      try (var paths = Files.walk(Path.of(args[0]))) {
        paths.filter(p -> p.toString().endsWith(".kt")).forEach(p -> {
          try {
            String source = Files.readString(p).replace("\r\n", "\n").replace("\r", "\n");
            var file = factory.createFile(p.getFileName().toString(), source);
            counts[0]++;
            for (var error : PsiTreeUtil.collectElementsOfType(file, PsiErrorElement.class)) {
              counts[1]++;
              int line = 1 + (int)source.substring(0, error.getTextOffset()).chars().filter(c -> c == '\n').count();
              if (counts[1] <= 30) System.out.println(p + ":" + line + ": " + error.getErrorDescription());
            }
          } catch (Exception e) { throw new RuntimeException(e); }
        });
      }
      System.out.println("Parsed " + counts[0] + " Kotlin files; " + counts[1] + " syntax errors. Android compilation is still required.");
      if (counts[1] > 0) System.exit(1);
    } finally { Disposer.dispose(disposable); }
  }
}
