package com.example.currency;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.Arrays;
import java.util.List;

@Route("currency-exchange")
@PageTitle("Currency Exchange Calculator")
public class CurrencyExchangeCalculatorView extends VerticalLayout {
    private final Properties exchangeRates = new Properties();

    public CurrencyExchangeCalculatorView() {
        List<String> currencies = Arrays.asList("USD", "EUR", "GBP", "JPY", "AUD", "CAD");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("exchange-rates.properties")) {
            if (in != null) {
                exchangeRates.load(in);
            }
        } catch (IOException e) {
            Notification.show("Failed to load exchange rates.");
        }

        NumberField amountField = new NumberField("Amount");
        ComboBox<String> fromCurrency = new ComboBox<>("From", currencies);
        ComboBox<String> toCurrency = new ComboBox<>("To", currencies);
        Button calculateButton = new Button("Calculate");

        calculateButton.addClickListener(event -> {
            Double amount = amountField.getValue();
            String from = fromCurrency.getValue();
            String to = toCurrency.getValue();
            if (amount == null || from == null || to == null) {
                Notification.show("Please fill all fields.");
                return;
            }
            if (from.equals(to)) {
                BigDecimal amountBD = BigDecimal.valueOf(amount);
                Money money = Money.of(CurrencyUnit.of(from), amountBD);
                Notification.show(money + " = " + money);
                return;
            }

            String key = from + "_" + to;
            String rateStr = exchangeRates.getProperty(key);
            if (rateStr == null) {
                Notification.show("Exchange rate not available for " + from + " to " + to);
                return;
            }
            try {
                BigDecimal rate = new BigDecimal(rateStr);
                BigDecimal amountBD = BigDecimal.valueOf(amount);
                BigDecimal converted = amountBD.multiply(rate);
                CurrencyUnit targetCurrency = CurrencyUnit.of(to);

                Money fromMoney = Money.of(CurrencyUnit.of(from), amountBD);
                Money toMoney = Money.of(targetCurrency, converted, java.math.RoundingMode.HALF_UP);

                Notification.show(fromMoney + " = " + toMoney);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage());
            }
        });

        add(amountField, fromCurrency, toCurrency, calculateButton);
    }
}
