# SD switch

The SD (Structured Data) switch provides access to the various services of Structured Data.
It can be configured with different `switches`, which are making use of the provided `recipes`.

## Configuration

General configuration of the SD switch happens using environment variables. Configuration of the switches using a YAML
file.

### Env variable configuration

The following environment variables are accepted:

- `SERVER_PORT`: The port to run the web server. Defaults to `8080`.
- `CONFIG`: The location of the YAML file for the configuration of the switches. Defaults to `config.yml` in
  the `resources`.

### Switch configuration

The YAML file to configure the switches looks as follows and expects:

```yaml
- recipe: nl.knaw.huc.sdswitch.recipe.helloworld.HelloWorldRecipe
  urls:
    - pattern: /test/{name}

- recipe: nl.knaw.huc.sdswitch.recipe.helloworld.HelloWorldRecipe
  urls:
    - pattern: /test2/{name}
```

- `recipe`: The class of the recipe to use.
- `urls`: A list of URLs on which to use the specified recipe. Each URL in the list is expected to have:
    - `pattern`: The URL pattern, it allows path parameters:
        - `{}`: Path parameter which does NOT allow slashes `/`.
        - `<>`: Path parameter which does allow slashes `/`.
        - `*`: Wildcard parameter.
    - `accept`: Optional: should only to be used if the request `Accept` header matches this.
- `config`: The configuration specific to the chosen recipe.

The configuration also allows the concept of `subSwitches`. This is useful when a lot of the configuration is shared
among different URL configurations, except for some small differences. An example using the `DreamFactoryRecipe`:

```yaml
- recipe: nl.knaw.huc.sdswitch.dreamfactory.DreamFactoryRecipe

  subSwitches:
    - urls:
        - pattern: /person/{id}
          accept: text/json
        - pattern: /person/{id}.json

      config:
        format: json

    - urls:
        - pattern: /person/{id}
          accept: text/ttl
        - pattern: /person/{id}.ttl

      config:
        format: ttl

    - urls:
        - pattern: /person/{id}
          accept: text/html
        - pattern: /person/{id}.html

      config:
        format: html

  config:
    type: postgres
    table: person
    baseUrl: http://localhost:8000
    apiKey: my-secret-api-key
    xml2HtmlPath: xml2html.xsl
    ttlSchemaPath: schema.xml
```

In this example, three sub switches are defined: one for each of the three available response types (json / ttl / html).
All the configuration is shared, except for the `format` property. This property is defined on each of the sub switches
instead.

## Recipe creation

Create a new recipe by implementing the interface `nl.knaw.huc.sdswitch.recipe.Recipe`. It is a generic interface with a
type `C` which maps to the configuration type. When the recipe is used, the configuration is automatically mapped to an
object of type `C`. Override the `validateConfig` method to perform some validation on the provided configuration and
the configured path parameters. The required method to override, `withData`, is called everytime a request comes in. It
provides access to the configuration and the HTTP request itself.

If the mapping from configuration to the matching object of type `C` cannot be done in a single go, use the
interface `nl.knaw.huc.sdswitch.recipe.ConfigMappingRecipe` instead. It provides an extra method to
override, `getConfig`, to map from an intermediate configuration object of type `M` to the configuration object of
type `C`.

To make the recipe available, provide the recipe in your `module-info.java`. Also allow access to your module
for `com.fasterxml.jackson.databind` so Jackson can map the configuration.

```java
provides nl.knaw.huc.sdswitch.recipe.Recipe with nl.knaw.huc.sdswitch.myrecipe.MyRecipe;
opens nl.knaw.huc.sdswitch.myrecipe to com.fasterxml.jackson.databind;
```

An example recipe `nl.knaw.huc.sdswitch.recipe.helloworld.HelloWorldRecipe` is provided:

```java
public class HelloWorldRecipe implements Recipe<Void> {
    @Override
    public void validateConfig(Void config, Set<String> pathParams) throws RecipeValidationException {
        if (!pathParams.contains("name"))
            throw new RecipeValidationException("Missing required path parameter 'name'");
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        return RecipeResponse.withBody("Hello " + data.pathParam("name"), "text/plain");
    }
}
```

## Available recipes

There are a number of recipes to choose from.

### ProviderRecipe

The `nl.knaw.huc.sdswitch.data.ProviderRecipe` provides access to files. Use it with the configuration:

- `provide`: The path to the file to provide. May include parameters `${}` from the `pattern`, if provided.
- `pattern`: An optional regular expression pattern to use on the `<path>` from the URL path.
- `contentType`: The content type of the file provided.

### RedirectRecipe

The `nl.knaw.huc.sdswitch.data.RedirectRecipe` redirects the request. Use it with the configuration:

- `redirectTo`: The URL to which to redirect the request. May include parameters `${}` from the `pattern`.
- `pattern`: The regular expression pattern on the `<path>` from the URL path.
- `isTempRedirect`: Whether it is a temporary redirect (HTTP 302) or not (HTTP 301). Defaults to `false`.

### DreamFactoryRecipe

The `nl.knaw.huc.sdswitch.dreamfactory.DreamFactoryRecipe` provides SQL data as HTML / JSON / TTL using
[DreamFactory](https://www.dreamfactory.com). Use it with the configuration:

- `type`: The DreamFactory type to use. (e.q. `postgres`)
- `table` The table to match in the database.
- `baseUrl`: The base URL where to find DreamFactory.
- `accept`: The Accept HTTP header to sent to DreamFactory in requests. Defaults to `application/json`.
- `apiKey`: The DreamFactory API key.
- `related`: A list of table columns with relations to obtain from DreamFactory as well.
- `format`: The format in which to render the data response (either `html`, `json` or `ttl`).
- `xml2HtmlPath`: The path to the XSLT file to use for serializing the data to HTML.
- `ttlSchemaPath`: The path to the TTL schema file to use for serializing the data to TTL.
