package com.example.calderon_calculadoravlsm;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    EditText ipInput, maskInput, hostsInput;
    Button calculateBtn;
    TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipInput = findViewById(R.id.ipInput);
        maskInput = findViewById(R.id.maskInput);
        hostsInput = findViewById(R.id.hostsInput);
        calculateBtn = findViewById(R.id.calculateBtn);
        resultText = findViewById(R.id.resultText);

        calculateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipInput.getText().toString().trim();
                String mask = maskInput.getText().toString().replace("/", "").trim();
                String[] hostsArray = hostsInput.getText().toString().split(",");
                List<Integer> hosts = new ArrayList<>();

                try {
                    for (String host : hostsArray) {
                        hosts.add(Integer.parseInt(host.trim()));
                    }

                    int prefijo = Integer.parseInt(mask);

                    if (prefijo < 0 || prefijo > 32) {
                        resultText.setText("Prefijo inválido. Debe estar entre 0 y 32.");
                        return;
                    }

                    String result = calcularVLSM(ip, hosts, prefijo);
                    resultText.setText(result);
                } catch (Exception e) {
                    resultText.setText("Error en los datos ingresados. Verifica IP, máscara y hosts.");
                }
            }
        });
    }

    private String calcularVLSM(String ipBase, List<Integer> hostsPorSubred, int prefijoInicial) {
        StringBuilder sb = new StringBuilder();
        int[] ip = convertirIPaIntArray(ipBase);
        hostsPorSubred.sort(Collections.reverseOrder());

        int currentIP = ipArrayToInt(ip);

        for (int i = 0; i < hostsPorSubred.size(); i++) {
            int hosts = hostsPorSubred.get(i);
            int bits = 32 - (int) Math.ceil(Math.log(hosts + 2) / Math.log(2));

            if (bits < prefijoInicial || bits > 32) {
                return "Error: No hay suficientes bits para la subred " + (i + 1) +
                        ". Prefijo inicial: /" + prefijoInicial + ", requerido: /" + bits;
            }

            int bloque = (int) Math.pow(2, 32 - bits);

            String subnetIP = intToIP(currentIP);
            String mascara = calcularMascara(bits);

            sb.append("Subred ").append(i + 1).append(":\n");
            sb.append("IP: ").append(subnetIP).append("\n");
            sb.append("Máscara: /").append(bits).append(" (").append(mascara).append(")\n");
            sb.append("Hosts requeridos: ").append(hosts).append("\n");
            sb.append("IPs totales en subred: ").append(bloque).append("\n");
            sb.append("Rango: ").append(intToIP(currentIP + 1)).append(" - ")
                    .append(intToIP(currentIP + bloque - 2)).append("\n");
            sb.append("Broadcast: ").append(intToIP(currentIP + bloque - 1)).append("\n\n");
            currentIP += bloque;
        }

        return sb.toString();
    }

    private int[] convertirIPaIntArray(String ip) {
        String[] partes = ip.split("\\.");
        int[] resultado = new int[4];
        for (int i = 0; i < 4; i++) {
            resultado[i] = Integer.parseInt(partes[i]);
        }
        return resultado;
    }

    private int ipArrayToInt(int[] ip) {
        return (ip[0] << 24) | (ip[1] << 16) | (ip[2] << 8) | ip[3];
    }

    private String intToIP(int ip) {
        return ((ip >> 24) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                (ip & 0xFF);
    }

    private String calcularMascara(int bits) {
        int mask = 0xffffffff << (32 - bits);
        return ((mask >> 24) & 0xff) + "." +
                ((mask >> 16) & 0xff) + "." +
                ((mask >> 8) & 0xff) + "." +
                (mask & 0xff);
    }
}
