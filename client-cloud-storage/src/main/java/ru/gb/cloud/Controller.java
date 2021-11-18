package ru.gb.cloud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class Controller implements Initializable {

    private final static String clientDir = "client-cloud-storage/Client-DATA";
    private static Path clientPath = Paths.get(clientDir);

    @FXML
    TextField pathFieldClient, pathFieldSrv, input;

    @FXML
    ListView<String> listViewClient, listViewSrv;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Message mess = Network.readObject();
                    if (mess instanceof FileMessage) {
                        FileMessage fileMessage = (FileMessage) mess;
                        Files.write(clientPath.resolve(fileMessage.getFilename()), fileMessage.getBuffer(), StandardOpenOption.CREATE);
                        refreshLocalFilesList(clientPath);
                    }
                    if (mess instanceof PathResponse) {
                        PathResponse pathResponse = (PathResponse) mess;
                        String path = pathResponse.getPath();
                        Platform.runLater(() -> pathFieldSrv.setText(path));
                    }
                    if (mess instanceof ListResponse) {
                        ListResponse listResponse = (ListResponse) mess;
                        List<String> list = listResponse.getList();
                        refreshServerFilesList(list);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        navigateOnDir();
        refreshLocalFilesList(clientPath);
    }

    public void refreshLocalFilesList(Path path) {
        pathFieldClient.setText(path.normalize().toAbsolutePath().toString());
        Platform.runLater(() -> {
            try {
                listViewClient.getItems().clear();
                listViewClient.getItems().addAll(
                        Files.list(path).map(p -> p.getFileName().toString()).collect(Collectors.toList())
                );
            } catch (IOException e) {
                log.error("Refresh Local list Error: ", e);
            }
        });
    }

    public void refreshServerFilesList(List<String> list) {
        Platform.runLater(() -> {
            listViewSrv.getItems().clear();
            listViewSrv.getItems().addAll(list);
        });

    }

    private void navigateOnDir() {
        try {
            listViewClient.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    input.setText(listViewClient.getSelectionModel().getSelectedItem());
                }
                if (event.getClickCount() == 2) {
                    Path path = clientPath.resolve(listViewClient.getSelectionModel().getSelectedItem());
                    if (Files.isDirectory(path)) {
                        clientPath = path;
                        refreshLocalFilesList(clientPath);
                    }
                }
            });

            listViewSrv.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    input.setText(listViewSrv.getSelectionModel().getSelectedItem());
                }
                if (event.getClickCount() == 2) {
                    Network.sendMsg(new PathRequestIn(listViewSrv.getSelectionModel().getSelectedItem()));
                }
            });
        } catch (Exception e) {
            log.error("Error ", e);
        }

    }

    public void btnPathClientUpAction(ActionEvent actionEvent) {
        Path path = Paths.get(pathFieldClient.getText()).getParent();
        if (path != null) {
            refreshLocalFilesList(path);
            clientPath = path;
        }
    }

    public void btnPathSrvUpAction(ActionEvent actionEvent) {
        Network.sendMsg(new PathRequestUp());
    }

    public void btnExitAction(ActionEvent actionEvent) {
        Network.stop();
        Platform.exit();
    }

    public void btnUploadAction(ActionEvent actionEvent) {
        try {
            FileMessage message = new FileMessage(clientPath.resolve(listViewClient.getSelectionModel().getSelectedItem()));
            Network.sendMsg(message);
        } catch (IOException e) {
            log.error("Error upload ", e);
        }
    }

    public void btnDownloadAction(ActionEvent actionEvent) {
        String fileName = listViewSrv.getSelectionModel().getSelectedItem();
        Network.sendMsg(new FileRequest(fileName));
    }

    public void btnDeleteAction(ActionEvent actionEvent) {
        //todo Delete Server
        if (listViewClient.getSelectionModel().getSelectedItem() != null) {
            Path path = Paths.get(pathFieldClient.getText()).resolve(listViewClient.getSelectionModel().getSelectedItem());
            File file = path.toFile();
            delete(file);
            refreshLocalFilesList(Paths.get(pathFieldClient.getText()));
        }
    }

    private void delete(File file) {
        File[] list = file.listFiles();
        if (list != null) {
            for (File f : list) {
                delete(f);
            }
        }
        file.delete();
    }
}