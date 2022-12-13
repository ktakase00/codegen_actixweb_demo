package org.openapitools.codegen;

import org.openapitools.codegen.*;
import org.openapitools.codegen.model.*;
import org.openapitools.codegen.languages.AbstractRustCodegen;
import org.openapitools.codegen.utils.ModelUtils;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.models.properties.*;
import joptsimple.internal.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.io.File;
import java.math.BigInteger;

import static org.openapitools.codegen.utils.StringUtils.camelize;
import static org.openapitools.codegen.utils.StringUtils.underscore;

public class ActixWebGenerator extends AbstractRustCodegen implements CodegenConfig {

    private final Logger LOGGER = LoggerFactory.getLogger(AbstractRustCodegen.class);

    // source folder where to write the files
    protected String sourceFolder = "src";
    protected String apiVersion = "1.0.0";

    private static final String uuidType = "uuid::Uuid";
    private static final String bytesType = "swagger::ByteArray";

    /**
    * Configures the type of generator.
    *
    * @return  the CodegenType for this generator
    * @see     org.openapitools.codegen.CodegenType
    */
    public CodegenType getTag() {
        return CodegenType.OTHER;
    }

    /**
    * Configures a friendly name for the generator.  This will be used by the generator
    * to select the library with the -g flag.
    *
    * @return the friendly name for the generator
    */
    public String getName() {
        return "actix-web";
    }

    public class RouteItem {
        public String method;
        public String operationId;

        RouteItem(CodegenOperation op) {
            this.method = op.httpMethod.toLowerCase();
            this.operationId = op.operationId;
        }
    }

    public class ResourceItem {
        public String resource;
        public List<RouteItem> routes;

        ResourceItem(String resource, List<RouteItem> routes) {
            this.resource = resource;
            this.routes = routes;
        }
    }

    /**
    * Provides an opportunity to inspect and modify operation data before the code is generated.
    */
    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {

        // to try debugging your code generator:
        // set a break point on the next line.
        // then debug the JUnit test called LaunchGeneratorInDebugger

        OperationsMap results = super.postProcessOperationsWithModels(objs, allModels);

        OperationMap ops = results.getOperations();
        List<CodegenOperation> opList = ops.getOperation();

        HashMap<String, List<RouteItem>> resourceMap = new HashMap<String, List<RouteItem>>();

        // iterate over the operation and perhaps modify something
        for(CodegenOperation co : opList){
            // example:
            // co.httpMethod = co.httpMethod.toLowerCase();

            if (!resourceMap.containsKey(co.path)) {
                List<RouteItem> routes = new ArrayList<RouteItem>();
                routes.add(new RouteItem(co));
                resourceMap.put(co.path, routes);
            }
            else {
                resourceMap.get(co.path).add(new RouteItem(co));
            }
        }

        List<ResourceItem> resourceList = new ArrayList<ResourceItem>();
        resourceMap.forEach((key, value) -> {
            resourceList.add(new ResourceItem(key, value));
        });
        this.vendorExtensions.put("x-resource-list", resourceList);

        return results;
    }

    /**
    * Returns human-friendly help for the generator.  Provide the consumer with help
    * tips, parameters here
    *
    * @return A string value for the help message
    */
    public String getHelp() {
        return "Generates a actix-web client library.";
    }

    public ActixWebGenerator() {
        super();

        // set the output folder here
        outputFolder = "generated-code/actix-web";

        /**
        * Models.  You can write model files using the modelTemplateFiles map.
        * if you want to create one template for file, you can do so here.
        * for multiple files for model, just put another entry in the `modelTemplateFiles` with
        * a different extension
        */
        modelTemplateFiles.put(
            "model.mustache", // the template to use
            ".rs");           // the extension for each file to write

        /**
        * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
        * as with models, add multiple entries with different extensions for multiple files per
        * class
        */
        apiTemplateFiles.put(
            "api.mustache",   // the template to use
            ".rs");           // the extension for each file to write

        /**
        * Template Location.  This is the location which templates will be read from.  The generator
        * will use the resource stream to attempt to read the templates.
        */
        templateDir = "actix-web";

        /**
        * Api Package.  Optional, if needed, this can be used in templates
        */
        apiPackage = "org.openapitools.api";

        /**
        * Model Package.  Optional, if needed, this can be used in templates
        */
        modelPackage = "org.openapitools.model";

        /**
        * Reserved words.  Override this with reserved words specific to your language
        */
        reservedWords = new HashSet<String> (
            Arrays.asList(
                "sample1",  // replace with static values
                "sample2")
        );

        /**
        * Additional Properties.  These values can be passed to the templates and
        * are available in models, apis, and supporting files
        */
        additionalProperties.put("apiVersion", apiVersion);

        /**
        * Supporting Files.  You can write single files for the generator with the
        * entire object tree available.  If the input file has a suffix of `.mustache
        * it will be processed by the template engine.  Otherwise, it will be copied
        */
        supportingFiles.add(new SupportingFile("myFile.mustache",   // the input template or file
            "",                                                     // the destination folder, relative `outputFolder`
            "myFile.sample")                                        // the output file
        );

        /**
        * Language Specific Primitives.  These types will not trigger imports by
        * the client generator
        */
        languageSpecificPrimitives = new HashSet<>(
                Arrays.asList(
                        "bool",
                        "char",
                        "i8",
                        "i16",
                        "i32",
                        "i64",
                        "u8",
                        "u16",
                        "u32",
                        "u64",
                        "isize",
                        "usize",
                        "f32",
                        "f64",
                        "str",
                        "String")
        );

        instantiationTypes.clear();
        instantiationTypes.put("array", "Vec");
        instantiationTypes.put("map", "std::collections::HashMap");

        typeMapping.clear();
        typeMapping.put("number", "f64");
        typeMapping.put("integer", "i32");
        typeMapping.put("long", "i64");
        typeMapping.put("float", "f32");
        typeMapping.put("double", "f64");
        typeMapping.put("string", "String");
        typeMapping.put("UUID", uuidType);
        typeMapping.put("URI", "String");
        typeMapping.put("byte", "u8");
        typeMapping.put("ByteArray", bytesType);
        typeMapping.put("binary", bytesType);
        typeMapping.put("boolean", "bool");
        typeMapping.put("date", "chrono::DateTime::<chrono::Utc>");
        typeMapping.put("DateTime", "chrono::DateTime::<chrono::Utc>");
        typeMapping.put("password", "String");
        typeMapping.put("File", bytesType);
        typeMapping.put("file", bytesType);
        typeMapping.put("array", "Vec");
        typeMapping.put("map", "std::collections::HashMap");
        typeMapping.put("object", "serde_json::Value");
        typeMapping.put("AnyType", "serde_json::Value");
    }

    /**
    * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
    * those terms here.  This logic is only called if a variable matches the reserved words
    *
    * @return the escaped term
    */
    @Override
    public String escapeReservedWord(String name) {
        return "_" + name;  // add an underscore to the name
    }

    /**
    * Location to write model files.  You can use the modelPackage() as defined when the class is
    * instantiated
    */
    public String modelFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar);
    }

    /**
    * Location to write api files.  You can use the apiPackage() as defined when the class is
    * instantiated
    */
    @Override
    public String apiFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar);
    }

    /**
    * override with any special text escaping logic to handle unsafe
    * characters so as to avoid code injection
    *
    * @param input String to be cleaned up
    * @return string with unsafe characters removed or escaped
    */
    @Override
    public String escapeUnsafeCharacters(String input) {
        //TODO: check that this logic is safe to escape unsafe characters to avoid code injection
        return input;
    }

    /**
    * Escape single and/or double quote to avoid code injection
    *
    * @param input String to be cleaned up
    * @return string with quotation mark removed or escaped
    */
    public String escapeQuotationMark(String input) {
        //TODO: check that this logic is safe to escape quotation mark to avoid code injection
        return input.replace("\"", "\\\"");
    }

    @Override
    public String toInstantiationType(Schema p) {
        if (ModelUtils.isArraySchema(p)) {
            ArraySchema ap = (ArraySchema) p;
            Schema inner = ap.getItems();
            return instantiationTypes.get("array") + "<" + getSchemaType(inner) + ">";
        } else if (ModelUtils.isMapSchema(p)) {
            Schema inner = getAdditionalProperties(p);
            return instantiationTypes.get("map") + "<" + typeMapping.get("string") + ", " + getSchemaType(inner) + ">";
        } else {
            return null;
        }
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        if (!languageSpecificPrimitives.contains(property.dataType)) {
            // If we use a more qualified model name, then only camelize the actual type, not the qualifier.
            if (property.dataType.contains(":")) {
                int position = property.dataType.lastIndexOf(":");
                property.dataType = property.dataType.substring(0, position) + camelize(property.dataType.substring(position));
            } else {
                property.dataType = camelize(property.dataType);
            }
            property.isPrimitiveType = property.isContainer && languageSpecificPrimitives.contains(typeMapping.get(property.complexType));
        } else {
            property.isPrimitiveType = true;
        }

        // Integer type fitting
        if (Objects.equals(property.baseType, "integer")) {

            BigInteger minimum = Optional.ofNullable(property.getMinimum()).map(BigInteger::new).orElse(null);
            BigInteger maximum =  Optional.ofNullable(property.getMaximum()).map(BigInteger::new).orElse(null);

            boolean unsigned = canFitIntoUnsigned(minimum, property.getExclusiveMinimum());

            if (Strings.isNullOrEmpty(property.dataFormat)) {
                property.dataType = bestFittingIntegerType(minimum,
                        property.getExclusiveMinimum(),
                        maximum,
                        property.getExclusiveMaximum(),
                        true);
            } else {
                switch (property.dataFormat) {
                    // custom integer formats (legacy)
                    case "uint32":
                        property.dataType = "u32";
                        break;
                    case "uint64":
                        property.dataType = "u64";
                        break;
                    case "int32":
                        property.dataType = unsigned ? "u32" : "i32";
                        break;
                    case "int64":
                        property.dataType = unsigned ? "u64" : "i64";
                        break;
                    default:
                        LOGGER.warn("The integer format '{}' is not recognized and will be ignored.", property.dataFormat);
                        property.dataType = bestFittingIntegerType(minimum,
                                property.getExclusiveMinimum(),
                                maximum,
                                property.getExclusiveMaximum(),
                                true);
                }
            }
        }

        property.name = underscore(property.name);

        if (!property.required) {
            property.defaultValue = (property.defaultValue != null) ? "Some(" + property.defaultValue + ")" : "None";
        }

        // If a property has no type defined in the schema, it can take values of any type.
        // This clashes with Rust being statically typed. Hence, assume it's sent as a json
        // blob and return the json value to the user of the API and let the user determine
        // the type from the value. If the property has no type, at this point it will have
        // baseType "object" allowing us to identify such properties. Moreover, set to not
        // nullable, we can use the serde_json::Value::Null enum variant.
        if ("object".equals(property.baseType)) {
            property.dataType = "serde_json::Value";
            property.isNullable = false;
        }
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, List<Server> servers) {
        Map<String, Schema> definitions = ModelUtils.getSchemas(this.openAPI);
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, servers);

        boolean hasPathParams = !op.pathParams.isEmpty();
        op.vendorExtensions.put("x-has-path-params", hasPathParams);

        op.vendorExtensions.put("x-action-create", httpMethod.equals("post") && !hasPathParams);
        op.vendorExtensions.put("x-action-list", httpMethod.equals("get") && !hasPathParams);
        op.vendorExtensions.put("x-action-update", httpMethod.equals("put") && hasPathParams);
        op.vendorExtensions.put("x-action-show", httpMethod.equals("get") && hasPathParams);

        op.vendorExtensions.put("x-has-request-params", httpMethod.equals("post") || httpMethod.equals("put"));
        return op;
    }

    @Override
    public CodegenModel fromModel(String name, Schema model) {
        LOGGER.trace("Creating model from schema: {}", model);

        Map<String, Schema> allDefinitions = ModelUtils.getSchemas(this.openAPI);
        CodegenModel mdl = super.fromModel(name, model);

        mdl.vendorExtensions.put("x-table-mapping", !name.endsWith("Request"));

        LOGGER.trace("Created model: {}", mdl);

        return mdl;
    }
}
