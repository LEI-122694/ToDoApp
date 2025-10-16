package com.example.taskchart;

import com.example.examplefeature.Task;
import com.example.examplefeature.TaskRepository;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H2;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@PageTitle("Upcoming Task Deadlines Chart")
@Route(value = "upcoming-tasks-chart", layout = com.example.base.ui.MainLayout.class)
public class UpcomingTasksChartView extends VerticalLayout {

    private final TaskRepository taskRepository;
    private final Div chartContainer = new Div();

    @Autowired
    public UpcomingTasksChartView(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;

        add(new H2("Upcoming Task Deadlines"));
        chartContainer.setId("tasksChart");
        chartContainer.setWidth("900px");
        chartContainer.setHeight("600px");
        add(chartContainer);

        setAlignItems(Alignment.CENTER);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        List<Task> tasks = taskRepository.findAll();

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        long todayCount = tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(today))
                .count();

        long next2DaysCount = tasks.stream()
                .filter(t -> t.getDueDate() != null &&
                        (t.getDueDate().isEqual(tomorrow) || t.getDueDate().isEqual(dayAfterTomorrow)))
                .count();

        long next3To7DaysCount = tasks.stream()
                .filter(t -> t.getDueDate() != null &&
                        t.getDueDate().isAfter(dayAfterTomorrow) &&
                        !t.getDueDate().isAfter(today.plusDays(7)))
                .count();

        long laterCount = tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(today.plusDays(7)))
                .count();

        // Labels e dados
        JsonArray labels = Json.createArray();
        labels.set(0, "Today");
        labels.set(1, "Tomorrow & Day After");
        labels.set(2, "Next 3–7 Days");
        labels.set(3, "Later");

        JsonArray data = Json.createArray();
        data.set(0, todayCount);
        data.set(1, next2DaysCount);
        data.set(2, next3To7DaysCount);
        data.set(3, laterCount);

        JsonObject dataset = Json.createObject();
        dataset.put("label", "Tasks by Due Date");
        dataset.put("data", data);
        dataset.put("backgroundColor", Json.create("[\"#36A2EB\", \"#FFCE56\", \"#FF6384\", \"#4BC0C0\"]"));

        JsonObject chartConfig = Json.createObject();
        chartConfig.put("type", "line");

        JsonObject dataObj = Json.createObject();
        dataObj.put("labels", labels);
        dataObj.put("datasets", Json.createArray());
        dataObj.getArray("datasets").set(0, dataset);
        chartConfig.put("data", dataObj);

        // Estilo moderno da linha
        dataset.put("borderColor", "#36A2EB");
        dataset.put("backgroundColor", "#36A2EB33");
        dataset.put("fill", true);
        dataset.put("tension", 0.4);
        dataset.put("pointBackgroundColor", "#36A2EB");
        dataset.put("pointBorderColor", "#fff");
        dataset.put("pointRadius", 6);

        // Configurações para eixo Y: números naturais
        JsonObject options = Json.createObject();
        JsonObject yAxis = Json.createObject();
        JsonObject ticks = Json.createObject();
        ticks.put("precision", 0);   // apenas inteiros
        yAxis.put("beginAtZero", true); // começa no 0
        yAxis.put("ticks", ticks);

        JsonObject scales = Json.createObject();
        scales.put("y", yAxis);
        options.put("scales", scales);

        chartConfig.put("options", options);

        // Carrega Chart.js e desenha
        getElement().executeJs(
                """
                const script = document.createElement('script');
                script.src = '/webjars/chart.js/4.5.0/dist/chart.umd.js';
                script.onload = () => {
                    const ctx = document.createElement('canvas');
                    $0.appendChild(ctx);
                    new Chart(ctx, $1);
                };
                document.head.appendChild(script);
                """,
                chartContainer, chartConfig
        );
    }
}
