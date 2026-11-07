# Headless Swing Testing Guide for Astrosoft (Java 21 LTS)

## Executing Headless UI Tests

Astrosoft runs on Java 21 LTS with modular flags:
```bash
ant test -Dtestcase=app.astrosoft.ui.test.LegacyScreensRegressionTest
```

Under the hood, Ant launches `org.junit.runner.JUnitCore` with:
- `--add-exports=java.desktop/sun.awt=ALL-UNNAMED`
- `--add-opens=java.desktop/sun.awt=ALL-UNNAMED`
- `--add-exports=java.desktop/sun.awt.shell=ALL-UNNAMED`
- `-Djava.awt.headless=true`

## Deep Layout & Offscreen Painting Pattern

To reliably test Swing components headlessly without physical display hardware, instantiate the component and render it into an offscreen `BufferedImage`:

```java
public static void renderOffscreen(Component comp, int width, int height) {
    comp.setSize(width, height);
    comp.doLayout();
    layoutDeep(comp);
    BufferedImage img = new BufferedImage(
        Math.max(1, width), 
        Math.max(1, height), 
        BufferedImage.TYPE_INT_RGB
    );
    Graphics2D g = img.createGraphics();
    comp.paint(g);
    g.dispose();
}

private static void layoutDeep(Component comp) {
    if (comp instanceof Container) {
        Container container = (Container) comp;
        container.doLayout();
        for (Component child : container.getComponents()) {
            layoutDeep(child);
        }
    }
}
```

## Traversing the Component Hierarchy

Use recursive component search to verify structural invariants:

```java
@SuppressWarnings("unchecked")
public static <T extends Component> List<T> findComponents(Container parent, Class<T> clazz) {
    List<T> list = new ArrayList<T>();
    for (Component c : parent.getComponents()) {
        if (clazz.isInstance(c)) {
            list.add((T) c);
        }
        if (c instanceof Container) {
            list.addAll(findComponents((Container) c, clazz));
        }
    }
    return list;
}
```

## Golden Rules for Headless Safety

1. **Never invoke `getLocationOnScreen()`** in constructor or initial layout paths; in headless mode this throws `HeadlessException`.
2. **Never call `JOptionPane.showConfirmDialog()`** directly on the event dispatch thread without checking `GraphicsEnvironment.isHeadless()` or mocking the user interaction.
3. **Always ensure `comp.setSize()` before `doLayout()`** in tests, otherwise preferred size calculations can default to 0x0 and fail assertion checks.
