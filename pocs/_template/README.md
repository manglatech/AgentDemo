# POC Template

Copy this folder to `pocs/<your-poc-id>/` and implement:

```java
@Component
public class YourPoc implements Poc {
    @Override
    public PocDescriptor descriptor() {
        return new PocDescriptor("your-poc-id", "Name", "Description", "pocs/your-poc-id");
    }

    @Override
    public PocRunResult execute() {
        // ...
    }
}
```
