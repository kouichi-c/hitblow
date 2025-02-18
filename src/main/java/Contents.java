import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
import java.text.Normalizer;

public class Contents {
    static Scanner scan = new Scanner(System.in);

    public static void main(String[] args) {//mainメソッド
        boolean retry = true;
        while (retry) {
            int ruleCount = 0;//ルール説明のミスカウント初期化
            int missCount = 0;
            int trial = 0;//試行回数のカウンタ―初期化
            String[][] history = new String[10][4];//10回分の入力履歴の配列を初期化
            System.out.println(showWelcomeMessage());
            while (rule(ruleCount)) {//falseになるまで繰り返す。
                ruleCount++;//カウントを+1する。
            }
            if (ruleCount <= 4) {
                game(trial, history, missCount);//ゲーム内容メソッドへ
            }
            retry = end();//ゲーム終了メソッドへ
        }
    }

    /**
     * hit & blow のタイトルを表示する
     */
    public static String showWelcomeMessage() {
        return readFile("welcome.txt");
    }

    /**
     * ルール説明を表示する
     */
    public static String showRule() {
        return readFile("rule.txt");
    }

    private static String readFile(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileReader fileReader = new FileReader(s)) {
            int text;
            while ((text = fileReader.read()) != -1) {
                stringBuilder.append((char) text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static boolean rule(int ruleCount) {
        System.out.println(ContentsConst.START_MESSAGE);
        String input = scan.nextLine();//入力フォーム。1または1,2以外で判定。2は特に処理が無い為そのままif文を通り過ぎる。
        input = input.replace(" ", "");//入力された内容からスペースを削除する。
        input = Normalizer.normalize(input, Normalizer.Form.NFKC);//全角英数字を半角英数字に変換。
        if (input.equals(ContentsConst.ONE)) {//1が入力された場合、ルール説明を表示。
            showRule();
        }
        if (!((input.equals(ContentsConst.ONE)) || (input.equals(ContentsConst.TWO)))) {//1か2以外はミス入力として処理。
            System.out.println(ContentsConst.CHOOSE_ONE_OR_TWO);
            //System.out.println(ruleCount);//＃確認用ミスカウント表示。
            if (ruleCount >= 5) {
                System.out.println(ContentsConst.WARNING_GAME_END);//ここから終了メソッドへ以降予定。
                return false;
            }
            return true;//ミス入力時は再度入力処理へ戻す為、同じメソッドを再度読み込みループ。
            //return ruleMiss(ruleCount);//5回ミスした場合はここの結果がfalseとなる為、ゲーム内容メソッドをスルーして終了メソッドへ移行して欲しい。
        }
        System.out.println(ContentsConst.START_HIT_BLOW);
        return false;
        //return true;
    }

    //ゲーム内容メソッド
    private static void game(int trial, String[][] history, int missCount) {
        int[] ans = createAnswerNumber();
        //System.out.println(Arrays.toString(ans));//＃確認用表示、3桁分の数字をランダムに設定、この時点で含まれる重複は以下で処理。
        while (ans[0] == ans[1]) {//1つめと2つめの数字が重複した場合は違う数字になるまで2つめの数値のランダムを繰り返す。
            ans[1] = getRandomNumber();
        }
        while (ans[0] == ans[2] || ans[1] == ans[2]) {//1つめと3つめ、2つめと3つめのどちらかが重複した場合は違う数字になるまで3つめの数値のランダムを繰り返す。
            ans[2] = getRandomNumber();
        }
        //System.out.println(Arrays.toString(ans));//＃確認用表示、重複がある場合は再抽選で重複が無くなる。
        input(trial, ans, history, missCount);//数値入力メソッドへ
    }

    private static int[] createAnswerNumber() {
        //各要素に乱数を10倍して少数以下を切り捨て、0～9までの数値となる。
        return Arrays.stream(new int[3]).map(i -> getRandomNumber()).toArray();
    }

    private static int getRandomNumber() {
        return (int) Math.floor(Math.random() * 10);
    }

    //数値入力メソッド
    private static void input(int trial, int[] ans, String[][] history, int missCount) {
        System.out.printf((ContentsConst.INPUT_MESSAGE) + "%n", trial);
        String input = scan.nextLine();//入力フォーム。3桁の整数、G、3桁以外の整数、整数やG以外の文字、3桁数字内の重複で判定。
        input = input.replace(" ", "");//入力された内容からスペースを削除する。
        input = input.toUpperCase();//小文字の英数字を大文字に変換。
        input = Normalizer.normalize(input, Normalizer.Form.NFKC);//全角英数字を半角英数字に変換。
        //System.out.println(input);//＃確認用表示、入力された内容が変換されているかの確認。
        if (input.equals("G")) {//ギブアップ選択時、ゲームオーバーメソッドに移行する。
            System.out.println(ContentsConst.CHOOSE_GIVE_UP);
            over(ans);//ゲームオーバーメソッドへ
            return;
        }
        int digit = input.length();
        if (digit != 3) {//数値の長さが3以外の場合はミス入力として処理。
            System.out.println(ContentsConst.INPUT_TREE_DIGIT);
            numMiss(trial, ans, history, missCount);//数値入力ミスメソッドへ
            return;//ミス回数が5回以上の場合は数値入力ミスメソッドで再読み込みがされない為、ここでメソッドを終了し、終了メソッドへ移行する。
        }
        if (!((input.matches("^[0-9]{3}$")) || (input.matches("^G$")))) {//数値0~9の3桁、Gの1桁以外の入力を弾く
            System.out.println(ContentsConst.INPUT_TREE_DIGIT);
            numMiss(trial, ans, history, missCount);//数値入力ミスメソッドへ
            return;
        }
        int[] prc = new int[3];//入力の配列、入力した3桁の数字を1桁ずつに分けて入れる。
        prc[0] = Integer.parseInt(input) / 100;//100の桁を取り出す、1/100を行い小数点切り捨て。
        prc[1] = Integer.parseInt(input) / 10 % 10;//10の桁を取り出す、1/10を行い、1/10の余りを取り出す。
        prc[2] = (int) Math.floor(Integer.parseInt(input) % 10);//1の桁を取り出す、1/10の余りを取り出す。
        //System.out.println(Arrays.toString(prc));//＃確認用表示、それぞれ格納した配列を表示。
        if (prc[0] == prc[1] || prc[0] == prc[2] || prc[1] == prc[2]) {//数値の重複確認、
            System.out.println(ContentsConst.WARNING_DUPLICATION);
            numMiss(trial, ans, history, missCount);//数値入力ミスメソッドへ
            return;
        }
        judge(prc, ans, history, trial, input);//判定メソッドへ
    }

    //判定メソッド
    private static void judge(int[] prc, int[] ans, String[][] history, int trial, String input) {
        trial++;//試行回数を1増やす
        int hit = 0;//ヒットのカウンタ―初期化
        int blow = 0;//ブローのカウンタ―初期化
        for (int i = 0; i < 3; i++) {//iが3に到達した時ループから脱出する。
            if (prc[i] == ans[i]) {//入力数値と正解数値のそれぞれ違う桁同士を比較し、数値が同じ違う桁がある毎にblowが1加算される。
                hit++;
            }
        }
        blow = getBlow(prc, ans, blow);//ブロー計算メソッドへ
        System.out.printf((ContentsConst.HIT) + "%n", hit);//桁も数字も合っている数。
        System.out.printf((ContentsConst.BLOW) + "%n", blow);//桁は合っていないが数字は合っている数、ヒットとの合計が3より上になる事は無い。
        if (hit == 3) {//hitが3桁全てである場合はゲームクリアとする。
            clear(ans, trial);//ゲームクリアメソッドへ
            return;
        }
        if (trial == 10) {//試行回数が10回目でゲームクリアに到達できない場合はゲームオーバーとする。
            System.out.println(ContentsConst.CANNOT_FOUND_CORRECT_NUMBER);
            over(ans);//ゲームオーバーメソッドへ
            return;
        }
        history[trial - 1][0] = String.valueOf(trial);
        history[trial - 1][1] = input;
        history[trial - 1][2] = String.valueOf(hit);
        history[trial - 1][3] = String.valueOf(blow);
        System.out.println(ContentsConst.RESULT_HEADER);
        for (int k = 0; k < trial; k++) {
            System.out.printf((ContentsConst.HISTORY) + "%n", history[k][0], history[k][1], history[k][2], history[k][3]);
        }
        input(trial, ans, history, 0);//再度数値入力メソッドから繰り返す。その際ミスカウントをリセットする。
    }

    //ブロー計算メソッド
    private static int getBlow(int[] prc, int[] ans, int blow) {
        for (int i = 0; i < 3; i++) {//iが3に到達した時ループから脱出する。
            for (int j = 0; j < 3; j++) {//jが3に到達した時ループから脱出し、iのカウンターを増やし再度カウントし直す。
                if (i != j) {//iとjが同数(ヒットと同じ)の場合は無視して次のループへ。
                    if (prc[i] == ans[j]) {//iとjが違う数字(桁の位置が違う)場合、お互いの要素の数値を比較し、数字が同じ場合はblowをを加算する。
                        blow++;
                    }
                }
            }
        }
        return blow;
    }

    //数値入力ミスメソッド
    private static void numMiss(int trial, int[] ans, String[][] history, int missCount) {
        missCount++;//カウントを+1する。
        //System.out.println(missCount);//＃確認用ミスカウント表示。
        if (missCount >= 5) {
            System.out.println(ContentsConst.WARNING_GAME_END);//＃確認用表示、ここから終了メソッドへ以降予定。
            return;
        }
        input(trial, ans, history, missCount);
    }

    //ゲームクリアメソッド
    private static void clear(int[] ans, int trial) {
        System.out.printf((ContentsConst.GAME_CLEAR), Arrays.toString(ans), trial);
    }

    //ゲームオーバーメソッド
    private static void over(int[] ans) {
        System.out.printf(ContentsConst.GAME_OVER, Arrays.toString(ans));
    }

    //ゲーム終了メソッド
    private static boolean end() {
        System.out.println(ContentsConst.CHOOSE_RETRY);
        String input = scan.nextLine();//入力フォーム。1または1,2以外で判定。
        input = input.replace(" ", "");//入力された内容からスペースを削除する。
        input = Normalizer.normalize(input, Normalizer.Form.NFKC);//全角英数字を半角英数字に変換。
        if (input.equals(ContentsConst.ONE)) {//1が入力された場合、再度メインメソッドを読み込む。
            System.out.println(ContentsConst.GAME_RETRY);
            return true;
        }
        if (input.equals(ContentsConst.TWO)) {//2が入力された場合、プログラムを終了する。
            System.out.println(ContentsConst.GAME_END);
        } else {//1か2以外はミス入力としてエラーメッセ―ジを挟み、2と同様にプログラムを終了する。
            System.out.println(ContentsConst.WARNING_GAME_END_2);
        }
        return false;
    }
}