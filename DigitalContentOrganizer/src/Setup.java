import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import models.*;

public class Setup {
    public static void create_tables() throws SQLException, IOException {
        try{
            User.create_table();
            Content.create_table();
            Shelf.create_table();
            Tag.create_table();
            ContentTag.create_table();
            ShelfTag.create_table();
            ContentShelf.create_table();
            ContentUser.create_table();
            ShelfUser.create_table();
        }catch(SQLException e){
            if(e.getMessage().contains("There is already an object named")){
                System.out.println(e.getMessage());
                return;
            }else{
                throw e;
            }
        }
        

        System.out.println("[*] Tables created");
    }

    public static void drop_tables() throws SQLException, IOException{
        ContentShelf.drop_table();
        ContentUser.drop_table();
        ShelfUser.drop_table();
        ShelfTag.drop_table();
        ContentTag.drop_table();
        Tag.drop_table();
        Shelf.drop_table();
        Content.drop_table();
        User.drop_table();
        
        System.out.println("[*] Tables dropped");
    }

    public static void create_admin(Scanner sc) throws SQLException, IOException{
        System.out.println("Username(admin):");
        String username = sc.nextLine();
        System.out.println("Password(admin):");
        String password = sc.nextLine();
        System.out.println("Email:");
        String email = sc.nextLine();

        if(email.isBlank()){
            System.out.println("[!] Email can't be blank");
        }
        if(username.isBlank()){
            username = "admin";
        }
        if(password.isBlank()){
            password = "admin";
        }
        System.out.println("username: " + username + " password: " + password + " email: " + email);
        try{
            User admin = new User();
            admin.username = username;
            admin.set_password(password);
            admin.email = email;
            admin.role = "admin";
            admin.insert();
        }catch(SQLException e){
            if(e.getMessage().contains("Violation of UNIQUE KEY constraint")){
                System.out.println("[!] Username/Email already exists.");
                return;
            }
            else{
                throw e;
            }
        }
        System.out.println("[*] Admin created");
    }

    public static void delete_account(Scanner sc) throws IOException, SQLException{
        System.out.println("Username:");
        String username = sc.nextLine();
        
    
        User user = User.get_by_username(username);
        user.username = username;
        user.delete();
        
        System.out.println("[*] Account deleted");
    }
    public static void main(String[] args) throws IOException, SQLException {
        // take input
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter 1 to create tables, 2 to drop tables, 3 to create an admin account, 4 to delete account, 0 to exit");
        int choice = sc.nextInt();
        sc.nextLine();
        
        switch(choice){
            case 0:
                System.out.println("Exiting...");
                sc.close();
                System.exit(0);
            case 1:
                create_tables();
                break;
            case 2:
                drop_tables();
                break;
            case 3:
                create_admin(sc);
                break;
            case 4:
                delete_account(sc);
                break;
            default:
                System.out.println("[!] Invalid input");
                break;
        }
        sc.close();
    }
}
