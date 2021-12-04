package com.rusefi;

import com.rusefi.enum_reader.Value;
import com.rusefi.util.SystemOut;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ToJavaEnum {
    public static void main(String[] args) throws IOException {
        InvokeReader invokeReader = new InvokeReader(args).invoke();
        String outputPath = invokeReader.getOutputPath();

        EnumsReader enumsReader = new EnumsReader();

        VariableRegistry registry = new VariableRegistry();
        for (String fileName : invokeReader.getDefinitionInputFiles())
            registry.readPrependValues(fileName);

        for (String inputFile : invokeReader.getInputFiles()) {
            File f = new File(invokeReader.getInputPath() + File.separator + inputFile);
            SystemOut.println("Reading enums from " + f);

            enumsReader.read(new FileReader(f));
        }

        for (Map.Entry<String /*enum name*/, EnumsReader.EnumState> e : enumsReader.getEnums().entrySet()) {
            String java = generate(registry, e.getKey(), e.getValue());

            String fullFileName = outputPath + File.separator + e.getKey() + ".java";
            BufferedWriter br = new BufferedWriter(new FileWriter(fullFileName));
            br.write(java);
            br.close();
        }
    }

    public static String generate(VariableRegistry registry, String key, EnumsReader.EnumState enumState) {
        StringBuilder sb = new StringBuilder("package com.rusefi.enums;\n");
        sb.append("//auto-generated by ToJavaEnum.java\n\n\n\n");
        sb.append("public enum " + key + " {\n");

        List<Value> sorted = EnumsReader.getSortedByOrder(registry, enumState.values);

        int index = 0;
        for (Value value : sorted) {
            int numericValue = value.getIntValueMaybeResolve(registry);
            if (index != numericValue && !value.getName().startsWith("Force_4_bytes_size"))
                throw new IllegalStateException("Got explicit ordinal " + numericValue + " instead of ordinal " + index + " in " + value);
            sb.append("\t" + value.getName() + ",\n");
            index++;
        }

        sb.append("}\n");
        return sb.toString();
    }
}