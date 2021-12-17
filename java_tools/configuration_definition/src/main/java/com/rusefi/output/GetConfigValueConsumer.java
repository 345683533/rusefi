package com.rusefi.output;

import com.rusefi.ConfigField;
import com.rusefi.ReaderState;
import com.rusefi.TypesHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;

import static com.rusefi.output.ConfigStructure.ALIGNMENT_FILL_AT;
import static com.rusefi.output.DataLogConsumer.UNUSED;

@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class GetConfigValueConsumer extends AbstractConfigurationConsumer {
    private static final String CONFIG_ENGINE_CONFIGURATION = "config->engineConfiguration.";
    private static final String ENGINE_CONFIGURATION = "engineConfiguration.";
    static final String FILE_HEADER = "#include \"pch.h\"\n";
    private static final String GET_METHOD_HEADER = "float getConfigValueByName(const char *name) {\n";
    static final String GET_METHOD_FOOTER = "\treturn EFI_ERROR_CODE;\n" + "}\n";
    private static final String SET_METHOD_HEADER = "void setConfigValueByName(const char *name, float value) {\n";
    private static final String SET_METHOD_FOOTER = "}\n";
    private final StringBuilder getterBody = new StringBuilder();
    private final StringBuilder setterBody = new StringBuilder();
    private final String outputFileName;

    public GetConfigValueConsumer(String outputFileName) {
        System.out.println("Hello " + getClass() + " " + outputFileName);
        this.outputFileName = outputFileName;
    }

    public static void writeStringToFile(@Nullable String fileName, String content) throws IOException {
        if (fileName != null) {
            FileWriter fw = new FileWriter(fileName);
            fw.write(content);
            fw.close();
        }
    }

    @Override
    public void handleEndStruct(ReaderState state, ConfigStructure structure) throws IOException {
        if (state.stack.isEmpty()) {
            PerFieldWithStructuresIterator iterator = new PerFieldWithStructuresIterator(state, structure.tsFields, "",
                    this::processConfig, ".");
            iterator.loop();
        }
    }

    @Override
    public void startFile() {
    }

    @Override
    public void endFile() throws IOException {
        writeStringToFile(outputFileName, getContent());
    }

    private String processConfig(ReaderState readerState, ConfigField cf, String prefix) {
        if (cf.getName().contains(UNUSED) || cf.getName().contains(ALIGNMENT_FILL_AT))
            return "";

        if (cf.isArray() || cf.isFromIterate() || cf.isDirective())
            return "";
        if (!TypesHelper.isPrimitive(cf.getType()) && !TypesHelper.isBoolean(cf.getType())) {
            return "";
        }

        String userName = prefix + cf.getName();
        if (userName.startsWith(ENGINE_CONFIGURATION))
            userName = userName.substring(ENGINE_CONFIGURATION.length());

        String javaName = "config->" + prefix;
        if (javaName.startsWith(CONFIG_ENGINE_CONFIGURATION))
            javaName = "engineConfiguration->" + javaName.substring(CONFIG_ENGINE_CONFIGURATION.length());

        getterBody.append(getCompareName(userName));
        getterBody.append("\t\treturn " + javaName + cf.getName() + ";\n");

        if (TypesHelper.isFloat(cf.getType())) {
            setterBody.append(getCompareName(userName));
            String str = getAssignment(cf, javaName, "");
            setterBody.append(str);
        } else {
            setterBody.append(getCompareName(userName));
            String str = getAssignment(cf, javaName, "(int)");
            setterBody.append(str);
        }


        return "";
    }

    @NotNull
    private String getAssignment(ConfigField cf, String javaName, String cast) {
        return "\t{\n" + "\t\t" + javaName + cf.getName() + " = " + cast +
                "value;\n" +
                "\t\treturn;\n\t}\n";
    }

    @NotNull
    static String getCompareName(String userName) {
        return "\tif (strEqualCaseInsensitive(name, \"" + userName + "\"))\n";
    }

    public String getGetterForUnitTest() {
        return FILE_HEADER +
                GET_METHOD_HEADER + getterBody + GET_METHOD_FOOTER;
    }

    public String getSetterBody() {
        return setterBody.toString();
    }

    public String getContent() {
        return FILE_HEADER +
                GET_METHOD_HEADER + getterBody + GET_METHOD_FOOTER
                +
                SET_METHOD_HEADER + setterBody + SET_METHOD_FOOTER
                ;
    }
}