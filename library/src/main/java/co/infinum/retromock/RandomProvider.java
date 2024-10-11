package co.infinum.retromock;

interface RandomProvider {

  long nextLong(long bound);

  int nextInt(int bound);

}
