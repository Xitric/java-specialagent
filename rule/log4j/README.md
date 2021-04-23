# SpecialAgent Rule for Apache Log4j 1.2

**Rule Name:** `log4j:1.2`

## Compatibility

```xml
<groupId>log4j</groupId>
<artifactId>log4j</artifactId>
<version>[1.2.4,1.2.17]</version>
```

## Configuration

There is currently one property used by the Log4j Rule.

`-Dsa.integration.log4j.logSpanIds`

  Activate to change the template used by a PatternLayout to also log trace and span ids.
  Valid inputs are 'true' and 'false'.
  
  **Default:** "false"
